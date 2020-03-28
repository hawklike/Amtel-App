package cz.prague.cvut.fit.steuejan.amtelapp.services

import android.app.IntentService
import android.content.Intent
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.RobinRoundTournament
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidMatch
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeams
import kotlinx.coroutines.runBlocking

class GenerateScheduleService : IntentService(GenerateScheduleService::class.simpleName)
{
    companion object
    {
        const val GROUP = "group"
        const val ROUNDS = "rounds"
        const val REGENERATE = "regenerate"
    }

    override fun onHandleIntent(intent: Intent)
    {
        val group = intent.getParcelableExtra<Group>(GROUP)
        val rounds = intent.getIntExtra(ROUNDS, 0)
        val regenerate = intent.getBooleanExtra(REGENERATE, false)

        runBlocking {
            if(regenerate) regenerateMatches(group, rounds)
            else generateMatches(group, rounds)
        }
    }

    private suspend fun generateMatches(group: Group, rounds: Int)
    {
        with(TeamManager.findTeams("groupId", group.id)) {
            if(this is ValidTeams)
            {
                try { createMatches(self, rounds, group) }
                catch(ex: Exception) { } //TODO: send error to crashlytics
            }
        }
    }

    private suspend fun regenerateMatches(group: Group, rounds: Int)
    {
        val year = DateUtil.actualYear
        var ok = true

        if(MatchManager.deleteAllMatches(group.id, year.toInt()))
        {
            group.teamIds[DateUtil.actualYear]?.forEach { teamId ->
                if(!clearTeamStatistics(teamId, year)) ok = false
            }
            if(ok) generateMatches(group, rounds)
        }
    }

    private suspend fun createMatches(teams: List<Team>, rounds: Int, group: Group)
    {
        val tournament = RobinRoundTournament()
        tournament.setTeams(teams)
        tournament.setRounds(rounds)
        tournament.createMatches(group).forEach {
            with(MatchManager.addMatch(it)) {
                if(this is ValidMatch) addMatchToTeams(self, teams)
            }
        }

        val map = group.rounds
        map[DateUtil.actualYear] = rounds
        GroupManager.updateGroup(group.id, mapOf("rounds" to map))
    }

    private suspend fun addMatchToTeams(match: Match, teams: List<Team>)
    {
        val homeTeam = teams.find { it.id == match.homeId }
        val awayTeam = teams.find { it.id == match.awayId }

        homeTeam?.let { TeamManager.addTeam(it) }
        awayTeam?.let { TeamManager.addTeam(it) }
    }

    private suspend fun clearTeamStatistics(teamId: String, year: String): Boolean
    {
        val team = TeamManager.findTeam(teamId)
        if(team is ValidTeam)
        {
            team.self.apply {
                pointsPerMatch[year] = mutableMapOf()
                pointsPerYear[year] = 0
                winsPerYear[year] = 0
                lossesPerYear[year] = 0
                matchesPerYear[year] = 0
                setsPositivePerMatch[year] = mutableMapOf()
                setsNegativePerMatch[year] = mutableMapOf()
                positiveSetsPerYear[year] = 0
                negativeSetsPerYear[year] = 0
            }
            return TeamManager.addTeam(team.self) != null
        }
        return false
    }

}