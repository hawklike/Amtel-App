package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.RankingOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Score


class RankingSolver(private val teams: List<Team>, private val year: Int)
{
    private var orderBy = RankingOrderBy.POINTS

    private val pointsAndTeams = mutableMapOf<Int, MutableList<Team>>()

    //list of Pair(Team, bonus points)
    private val overallRanking = mutableListOf<Team>()

    fun withOrder(orderBy: RankingOrderBy): RankingSolver
    {
        this.orderBy = orderBy
        return this
    }

    //TODO: return List<Team>
    suspend fun sort(): List<Team>
    {
        sortByPoints()
        TODO()
        return overallRanking
    }

    private suspend fun sortByPoints()
    {
        setPointsAndTeams()
        setOverallRanking()
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

    /*
    The function sorts map by points (descending), then checks number of teams with
    the same number of points, if there is only one team, inputs the team
    into overallResults list, otherwise determines ranking of the teams and
    inputs them later
    */
    private suspend fun setOverallRanking()
    {
        pointsAndTeams.toSortedMap(reverseOrder()).values.forEach { teams ->
            if(teams.size == 1) overallRanking.add(teams.first())
            else overallRanking.addAll(diffTeamsWithSamePoints(teams))
        }
    }

    private suspend fun diffTeamsWithSamePoints(teams: MutableList<Team>): List<Team>
    {
        //(teamId, score)
        var results = mutableMapOf<Team, Score>()
        val matches = getMatches(teams)

        teams.forEach { team ->
            results[team] = Score()
        }

        //already sorted by score
        results = setResults(results, matches).toMutableMap()
        return checkResults(results)

//        return results.keys.toList()
//
//        results.keys.forEach { team ->
//            overallRanking.add(team)
//        }
    }

    private suspend fun checkResults(results: MutableMap<Team, Score>): List<Team>
    {
        val teamsWithSameScore = mutableSetOf<Team>()
        var lastScore = Score()
        var previousTeam: Team? = null

        for((team, score) in results)
        {
            if(score != Score() && score == lastScore)
            {
                previousTeam?.let { teamsWithSameScore.add(it) }
                teamsWithSameScore.add(team)
            }

            lastScore = score
            previousTeam = team
        }

        return if(teamsWithSameScore.isNotEmpty())
        {
            val teams = results.keys.toList()
            val leftIndex = teams.indexOf(teamsWithSameScore.first())
            val rightIndex = teams.indexOf(teamsWithSameScore.last())
            val preTeams = teams.take(leftIndex)
            val postTeams = teams.drop(rightIndex + 1)
            preTeams + diffTeamsWithSamePoints(teamsWithSameScore.toMutableList()) + postTeams
        }
        else results.keys.toList()
    }

    //is working
    private fun setResults(results: MutableMap<Team, Score>, matches: List<Match>): Map<Team, Score>
    {
        matches.forEach { match ->
            val homeTeam = results.keys.find { it.id == match.homeId }
            homeTeam?.let { team ->
                val homeScore = results[team]!!
                homeScore.sets += match.rounds.sumBy { it.homeSets ?: 0 }
                homeScore.games += match.rounds.sumBy { it.homeGems ?: 0 }
                results[team] = homeScore
            }

            val awayTeam = results.keys.find { it.id == match.awayId }
            awayTeam?.let { team ->
                val awayScore = results[team]!!
                awayScore.sets += match.rounds.sumBy { it.awaySets ?: 0 }
                awayScore.games += match.rounds.sumBy { it.awayGems ?: 0 }
                results[team] = awayScore
            }
        }

        //sort by values (score, i.e the more sets gained, the better it is)
        return results.toList().sortedBy{ it.second }.toMap()
    }

    //is working
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
