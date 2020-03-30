package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

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

class ScheduleGenerator
{
    suspend fun generateMatches(group: Group, rounds: Int): Boolean
    {
        with(TeamManager.findTeams("groupId", group.id)) {
            return if(this is ValidTeams)
            {
                try
                {
                    createMatches(self, rounds, group)
                    true
                }
                catch(ex: Exception) { false } //TODO: send error to crashlytics
            }
            else false
        }
    }

    suspend fun regenerateMatches(group: Group, rounds: Int): Boolean
    {
        val year = DateUtil.actualYear
        var ok = true

        return if(MatchManager.deleteAllMatches(group.id, year.toInt()))
        {
            group.teamIds[DateUtil.actualYear]?.forEach { teamId ->
                if(!clearTeamStatistics(teamId, year)) ok = false
            }
            if(ok) generateMatches(group, rounds)
            else false
        }
        else false
    }

    private suspend fun createMatches(teams: List<Team>, rounds: Int, group: Group)
    {
        val tournament = RobinRoundTournament()
        tournament.setTeams(teams)
        tournament.setRounds(rounds)
        tournament.createMatches(group).forEach {
            with(MatchManager.setMatch(it)) {
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

        homeTeam?.let { TeamManager.setTeam(it) }
        awayTeam?.let { TeamManager.setTeam(it) }
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
            return TeamManager.setTeam(team.self) != null
        }
        return false
    }
}