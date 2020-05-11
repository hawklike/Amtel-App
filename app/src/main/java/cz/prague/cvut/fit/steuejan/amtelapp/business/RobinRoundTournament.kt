package cz.prague.cvut.fit.steuejan.amtelapp.business

import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team

class RobinRoundTournament
{
    //list of pairs of team ID (doesn't match Cloud Firestore ID) and team, if ID == -1, then it is a dummy team
    private val teams: MutableList<Pair<Int, Team>> = mutableListOf()
    private var rounds = 0
    private val matches: MutableList<Match> = mutableListOf()

    private val homes by lazy {
        teams.take(teams.size / 2).toMutableList()
    }

    private val aways by lazy {
        teams.drop(teams.size / 2).reversed().toMutableList()
    }

    fun setTeams(teams: List<Team>)
    {
        //add dummy team if number of teams is odd
        if(teams.size % 2 != 0) this.teams.add(Pair(-1, Team()))
        //create id of a team
        (1..teams.size).map { this.teams.add(Pair(it, teams[it - 1])) }
        //update number of rounds based on number of teams
        rounds = if(this.teams.size % 2 == 0) (this.teams.size - 1) else this.teams.size
    }

    fun setRounds(n: Int)
    {
        //add dummy teams to equalize even number of teams
        (1..(n - rounds)).map { teams.add(Pair(-1, Team())) }
        rounds = n
    }

    fun createMatches(group: Group): List<Match>
    {
        for(round: Int in 1..rounds)
        {
            for(i: Int in 0 until homes.size)
            {
                val home: Pair<Int, Team> = homes[i]
                val away: Pair<Int, Team> = aways[i]
                //if there is not a dummy team...
                if (home.first != -1 && away.first != -1)
                {
                    val a: Team = max(home, away)
                    val b: Team = min(home, away)
                    //in order to set the best home/away matches ratio
                    if((home.first + away.first) % 2 == 0)
                        matches.add(Match(null, group.id!!, group.name, round, a.name, b.name, a.id!!, b.id!!, place = a.place, playOff = group.playOff))
                    else
                        matches.add(Match(null, group.id!!, group.name, round, b.name, a.name, b.id!!, a.id!!, place = b.place, playOff = group.playOff))
                }
            }
            if(rounds > 1) rotate()
        }

        return matches
    }

    //retrieves team with higher ID
    private fun max(a: Pair<Int, Team>, b: Pair<Int, Team>): Team
    {
        return if(a.first >= b.first) a.second
        else b.second
    }

    //retrieves team with lower ID
    private fun min(a: Pair<Int, Team>, b: Pair<Int, Team>): Team
    {
        return if(a.first <= b.first) a.second
        else b.second
    }

    //rotate according to Scheduling algorithm (https://en.wikipedia.org/wiki/Round-robin_tournament)
    private fun rotate()
    {
        val lastHome = homes.removeAt(homes.lastIndex)
        val firstAway = aways.removeAt(0)

        homes.add(1, firstAway)
        aways.add(lastHome)
    }
}