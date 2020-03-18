package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.RankingOrderBy

class RankingSolver(private val teams: List<Team>, private val year: Int)
{
    private var orderBy = RankingOrderBy.POINTS

    private val pointsAndTeams = mutableMapOf<Int, MutableList<Team>>()

    //list of Pair(Team, bonus points)
    private val weightedRanking = mutableListOf<Pair<Team, Int>>()

    fun withOrder(orderBy: RankingOrderBy): RankingSolver
    {
        this.orderBy = orderBy
        return this
    }

    //TODO: return List<Team>
    suspend fun sort()
    {
        sortByPoints()
    }

    private suspend fun sortByPoints()
    {
        setPointsAndTeams()
        setWeightedRanking()
    }

    //is working
    private fun setPointsAndTeams()
    {
        teams.forEach { team ->
            team.pointsPerYear[year.toString()]?.let { points ->
                val tmp = pointsAndTeams[points]
                if(tmp == null) pointsAndTeams[points] = mutableListOf()

                pointsAndTeams[points]!!.add(team)
            }
        }
    }

    //is working
    private suspend fun setWeightedRanking()
    {
        pointsAndTeams.values.forEach { teams ->
            if(teams.size == 1) weightedRanking.add((Pair(teams.first(), 0)))
            else diffTeamWithSamePoints(teams)
        }
    }

    private suspend fun diffTeamWithSamePoints(teams: MutableList<Team>)
    {
        val matches = getMatches(teams)
        TODO()


    }

    private suspend fun getMatches(teams: MutableList<Team>): List<Match>
    {
        val combinedTeams = mutableListOf<Pair<Team, Team>>()
        for(i in 0 until teams.size)
        {
            for(j in i+1 until teams.size)
                combinedTeams.add(Pair(teams[i], teams[j]))
        }

        val matches = mutableListOf<Match>()
        combinedTeams.forEach {
            matches.addAll(MatchManager.getCommonMatches(it.first, it.second, year))
        }

        return matches
    }

}
