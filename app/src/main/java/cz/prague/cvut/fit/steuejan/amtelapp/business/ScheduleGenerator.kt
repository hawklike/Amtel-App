package cz.prague.cvut.fit.steuejan.amtelapp.business

import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.TestingUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.GroupRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.MatchRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidMatch
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeams

/*
Generates a timetable of matches.
 */
class ScheduleGenerator
{
    suspend fun generateMatches(group: Group, rounds: Int): Boolean
    {
        with(TeamRepository.findTeams("groupId", group.id)) {
            return if(this is ValidTeams)
            {
                try
                {
                    //successfully found teams in database, create a timetable
                    createMatches(self, rounds, group)
                    true
                }
                catch(ex: Exception)
                {
                    with(TestingUtil) {
                        log("ScheduleGenerator::generateMatches(): $rounds (all) matches in group $group not generated because ${ex.message}")
                        throwNonFatal(ex)
                    }
                    false
                }
            }
            else false
        }
    }

    //delete all matches already generated and create new ones
    suspend fun regenerateMatches(group: Group, rounds: Int): Boolean
    {
        val year = DateUtil.actualSeason
        var ok = true

        //delete all matches in the group and an actual season
        return if(MatchRepository.deleteAllMatches(group.id, year.toInt()))
        {
            group.teamIds[DateUtil.actualSeason]?.forEach { teamId ->
                //clear team statistics for the actual season
                if(!clearTeamStatistics(teamId, year)) ok = false
            }
            //create new matches
            if(ok) generateMatches(group, rounds)
            else false
        }
        else false
    }

    private suspend fun createMatches(teams: List<Team>, rounds: Int, group: Group)
    {
        //matches are generated as robin round tournament
        val tournament = RobinRoundTournament()
        tournament.setTeams(teams)
        tournament.setRounds(rounds)
        tournament.createMatches(group).forEach {
            //input a match in database
            with(MatchRepository.setMatch(it)) {
                //add match to proper teams
                if(this is ValidMatch) addMatchToTeams(self, teams)
            }
        }

        val map = group.rounds
        map[DateUtil.actualSeason] = rounds
        GroupRepository.updateGroup(group.id, mapOf("rounds" to map))
    }

    private suspend fun addMatchToTeams(match: Match, teams: List<Team>)
    {
        val homeTeam = teams.find { it.id == match.homeId }
        val awayTeam = teams.find { it.id == match.awayId }

        //update teams in database
        homeTeam?.let { TeamRepository.setTeam(it) }
        awayTeam?.let { TeamRepository.setTeam(it) }
    }

    private suspend fun clearTeamStatistics(teamId: String, year: String): Boolean
    {
        val team = TeamRepository.findTeam(teamId)
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
            return TeamRepository.setTeam(team.self) != null
        }
        return false
    }
}