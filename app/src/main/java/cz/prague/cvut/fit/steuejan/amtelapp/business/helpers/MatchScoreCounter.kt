package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team

class MatchScoreCounter(private val homeTeam: Team, private val awayTeam: Team)
{
    suspend fun countTotalScore(match: Match, isDefaultLoss: Boolean = false)
    {
        var homeScore = 0
        var awayScore = 0

        match.rounds.forEach {
            if(it.homeWinner == true) homeScore++
            else if(it.homeWinner == false) awayScore++
        }

        if(homeScore + awayScore == 0)
        {
            if(match.homeScore != null || match.awayScore != null)
            {
                match.homeScore = null
                match.awayScore = null
                MatchManager.setMatch(match)
            }
        }
        else
        {
            if(match.homeScore != homeScore || match.awayScore != awayScore)
            {
                match.homeScore = homeScore
                match.awayScore = awayScore
                MatchManager.setMatch(match)
                updatePoints(match, isDefaultLoss)
            }
        }
    }

    private suspend fun updatePoints(match: Match, isDefaultLoss: Boolean)
    {
        updatePoints(homeTeam, match, isDefaultLoss) { match.homeScore!! > match.awayScore!! }
        updatePoints(awayTeam, match, isDefaultLoss) { match.awayScore!! > match.homeScore!! }
    }

    private suspend fun updatePoints(team: Team, match: Match, isDefaultLoss: Boolean, isWinner: () -> Boolean)
    {
        val year = DateUtil.actualSeason

        val pointsPerYear = team.pointsPerMatch[year]
        if(pointsPerYear == null) team.pointsPerMatch[year] = mutableMapOf()

        var sum = 0
        var wins = 0
        team.pointsPerMatch[year]!!.let { points ->
            points[match.id!!] = when
            {
                isWinner.invoke() -> App.POINTS_WIN
                isDefaultLoss -> App.POINTS_DEFAULT_LOSS
                else -> App.POINTS_LOOSE
            }
            sum = points.values.sum()
            points.values.forEach { if(it == App.POINTS_WIN) wins++ }
        }

        team.pointsPerYear[year] = sum
        team.winsPerYear[year] = wins
        team.lossesPerYear[year] = team.pointsPerMatch[year]!!.size - wins
        team.matchesPerYear[year] = team.pointsPerMatch[year]!!.size

        initSetsStatistics(team, match)

        TeamManager.setTeam(team)
    }

    private fun initSetsStatistics(team: Team, match: Match)
    {
        val year = DateUtil.actualSeason

        val positiveSetsPerYear = team.setsPositivePerMatch[year]
        if(positiveSetsPerYear == null) team.setsPositivePerMatch[year] = mutableMapOf()

        val negativeSetsPerYear = team.setsNegativePerMatch[year]
        if(negativeSetsPerYear == null) team.setsNegativePerMatch[year] = mutableMapOf()

        team.setsPositivePerMatch[year]!!.let { sets ->
            sets[match.id!!] = when(team.id)
            {
                match.homeId -> match.rounds.fold(0) { acc, round -> round.homeSets?.let { acc + it } ?: acc }
                else -> match.rounds.fold(0) { acc, round -> round.awaySets?.let { acc + it } ?: acc }
            }
            team.positiveSetsPerYear[year] = sets.values.sum()
        }

        team.setsNegativePerMatch[year]!!.let { sets ->
            sets[match.id!!] = when(team.id)
            {
                match.homeId -> match.rounds.fold(0) { acc, round -> round.awaySets?.let { acc + it } ?: acc }
                else -> match.rounds.fold(0) { acc, round -> round.homeSets?.let { acc + it } ?: acc }
            }
            team.negativeSetsPerYear[year] = sets.values.sum()
        }
    }
}