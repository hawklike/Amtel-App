package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.RankingOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Score


class RankingSolver(private val teams: List<Team>, private val year: Int)
{
    private val mYear = year.toString()
    private var orderBy = RankingOrderBy.POINTS

    private val pointsAndTeams = mutableMapOf<Int, MutableList<Team>>()
    private val overallRanking = mutableListOf<Team>()

    fun withOrder(orderBy: RankingOrderBy): RankingSolver
    {
        this.orderBy = orderBy
        return this
    }

    suspend fun sort(): List<Team>
    {
        return when(orderBy)
        {
            RankingOrderBy.POINTS -> sortByPoints()
            RankingOrderBy.MATCHES -> teams.sortedByDescending { it.matchesPerYear[mYear] }
            RankingOrderBy.WINS -> sortByPoints()
            RankingOrderBy.LOSSES -> sortByPoints().reversed()
            RankingOrderBy.POSITIVE_SETS -> teams.sortedByDescending { it.positiveSetsPerYear[mYear] }
            RankingOrderBy.NEGATIVE_SETS -> teams.sortedByDescending { it.negativeSetsPerYear[mYear] }
        }
    }

    private suspend fun sortByPoints(): List<Team>
    {
        setPointsAndTeams()
        setOverallRanking()
        return overallRanking
    }

    /*
    Divide teams by points.
     */
    private fun setPointsAndTeams()
    {
        teams.forEach { team ->
            team.pointsPerYear[mYear]?.let { points ->
                val tmp = pointsAndTeams[points]
                if(tmp == null) pointsAndTeams[points] = mutableListOf()

                pointsAndTeams[points]!!.add(team)
            } ?: let {
                val tmp = pointsAndTeams[0]
                if(tmp == null) pointsAndTeams[0] = mutableListOf()

                pointsAndTeams[0]!!.add(team)
            }
        }
    }

    /*
    The function sorts the map of pointsAndTeams by points (descending), then checks
    number of teams with the same number of points, if there is only one team,
    inputs the team into the overallResults list, otherwise determines
    ranking of the teams and inputs them later on.
    */
    private suspend fun setOverallRanking()
    {
        for((key, value) in pointsAndTeams.toSortedMap(reverseOrder()))
        {
            when
            {
                value.size == 1 -> overallRanking.add(value.first())
                key == 0 -> overallRanking.addAll(value)
                else -> overallRanking.addAll(diffTeamsWithSamePoints(value))
            }
        }
    }

    /*
    As the name of the function says, this function differs teams with the same
    number of points according to these rules:
            1. if there are only two same ranked teams, divide them by their common match
            2. otherwise, create small table (table of sets and games gained among other
               same ranked teams) and divide them by this table
     */
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
    }

    /*
    Checks if two and more teams don't have the same number of sets and games.
    If so, calls the diffTeamsWithSamePoints function again with the same ranked teams
    and the result merges with other teams that are in the right order.
     */
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

        val teams = results.keys.toList()
        return if(teamsWithSameScore.isNotEmpty())
        {
            val leftIndex = teams.indexOf(teamsWithSameScore.first())
            val rightIndex = teams.indexOf(teamsWithSameScore.last())
            teams.take(leftIndex) + diffTeamsWithSamePoints(teamsWithSameScore.toMutableList()) + teams.drop(rightIndex + 1)
        }
        else teams
    }

    /*
    Create a score for each team in the results map. Score is a pair of total sets and games
    got in the matches. After that, the function sort the map of results according to
    first sets and then games.
     */
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


    /*
    Returns matches of the teams given as a parameter.
     */
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
