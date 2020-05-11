package cz.prague.cvut.fit.steuejan.amtelapp.business

import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.POINTS_DEFAULT_LOSS
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.POINTS_LOOSE
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.POINTS_WIN
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.MatchRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository

/*
This class is responsible for counting final score of a match (three rounds)
 */
class MatchScoreCounter(private val match: Match, private val homeTeam: Team, private val awayTeam: Team)
{
    private val playoff = match.playOff
    private var isDefaultLoss = false

    fun withDefaultLoss(isDefaultLoss: Boolean): MatchScoreCounter
    {
        this.isDefaultLoss = isDefaultLoss
        return this
    }

    //update final score of a match in the database
    suspend fun countTotalScore()
    {
        var homeScore = 0
        var awayScore = 0

        match.rounds.forEach {
            if(it.homeWinner == true) homeScore++
            else if(it.homeWinner == false) awayScore++
        }

        if(homeScore + awayScore == 0)
        {
            //match not played or all rounds are ties
            match.homeScore = null
            match.awayScore = null
        }
        else
        {
            match.homeScore = homeScore
            match.awayScore = awayScore
        }

        //update match in database
        MatchRepository.setMatch(match)
        if(playoff) resolvePlayoff(homeScore, awayScore)
        updatePoints(homeScore, awayScore)
    }

    private suspend fun updatePoints(homeScore: Int, awayScore: Int)
    {
        updatePoints(homeTeam) { homeScore > awayScore }
        updatePoints(awayTeam) { awayScore > homeScore }
    }

    private suspend fun updatePoints(team: Team, isWinner: () -> Boolean)
    {
        val year =
            if(playoff) 0.toString() //if playoff, don't count to an actual season
            else match.year.toString()

        val pointsPerYear = team.pointsPerMatch[year]
        if(pointsPerYear == null) team.pointsPerMatch[year] = mutableMapOf() //create a key if not existing

        var sum = 0
        var wins = 0
        //resolve how many points team got in a match
        team.pointsPerMatch[year]!!.let { points ->
            //number of points got in a match
            points[match.id!!] = when
            {
                isWinner.invoke() -> POINTS_WIN
                isDefaultLoss -> POINTS_DEFAULT_LOSS
                else -> POINTS_LOOSE
            }
            sum = points.values.sum()
            points.values.forEach { if(it == POINTS_WIN) wins++ }
        }

        //update team statistics
        team.pointsPerYear[year] = sum
        team.winsPerYear[year] = wins
        team.lossesPerYear[year] = team.pointsPerMatch[year]!!.size - wins
        team.matchesPerYear[year] = team.pointsPerMatch[year]!!.size

        initSetsStatistics(team)
        //update team in database
        TeamRepository.setTeam(team)
    }

    private suspend fun resolvePlayoff(homeScore: Int, awayScore: Int)
    {
        val inputter = TeamToGroupInputter()
            .isPlayoff(true)
        if(homeScore < awayScore)
        {
            //home team loses and therefore goes to a lower group in the next season
            inputter.addToGroup(homeTeam, match.worseGroup)
            inputter.addToGroup(awayTeam, match.betterGroup)
        }
        else
        {
            //home team wins and therefore stays in the same group in the next season
            inputter.addToGroup(homeTeam, match.betterGroup)
            inputter.addToGroup(awayTeam, match.worseGroup)
        }
    }

    private fun initSetsStatistics(team: Team)
    {
        val year =
            if(playoff) 0.toString()
            else match.year.toString()

        val positiveSetsPerYear = team.setsPositivePerMatch[year]
        if(positiveSetsPerYear == null) team.setsPositivePerMatch[year] = mutableMapOf()

        val negativeSetsPerYear = team.setsNegativePerMatch[year]
        if(negativeSetsPerYear == null) team.setsNegativePerMatch[year] = mutableMapOf()

        //count positive sets for the match
        team.setsPositivePerMatch[year]!!.let { sets ->
            sets[match.id!!] = when(team.id)
            {
                match.homeId -> match.rounds.fold(0) { acc, round -> round.homeSets?.let { acc + it } ?: acc }
                else -> match.rounds.fold(0) { acc, round -> round.awaySets?.let { acc + it } ?: acc }
            }
            team.positiveSetsPerYear[year] = sets.values.sum()
        }

        //count negative sets for the match
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