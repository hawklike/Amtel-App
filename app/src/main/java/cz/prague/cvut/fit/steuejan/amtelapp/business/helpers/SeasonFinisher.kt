package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.LeagueManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroup
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroups
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidMatch
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeams

class SeasonFinisher(private val groups: MutableList<Group>)
{
    private val groupsWithSortedTeams = mutableMapOf<Group, List<Team>>()

    private val groupsPlayingPlayoff by lazy { groups.filter { it.playingPlayOff } }

    private val playoff = Group("playoff", context.getString(R.string.playOff), playingPlayOff = false, playOff = true, rank = Int.MAX_VALUE)

    private val resolvedTeamIds = mutableSetOf<String>()
    private val sortedGroups = mutableListOf<Group>()

    private var actualSeason = 0

    suspend fun createPlayoff(): Boolean
            = GroupManager.addPlayoff(playoff) is ValidGroup

    suspend fun updateTeamRanks()
    {
        getActualSeasonIf { actualSeason == 0 }
        getGroupsIf { groups.isEmpty() }
        prepareGroupsIf { sortedGroups.isEmpty() }

        groups.forEach { group ->
            group.teamIds[(actualSeason + 1).toString()] = mutableListOf()
            with(GroupManager.retrieveTeamsInGroup(group.id)) {
                if(this is ValidTeams)
                {
                    val rankedTeams = RankingSolver(self, actualSeason).sort()
                    groupsWithSortedTeams[group] = rankedTeams
                    for(i in rankedTeams.indices)
                    {
                        rankedTeams[i].results.add(i+1)
                        TeamManager.setTeam(rankedTeams[i])
                    }
                }
            }
        }
    }

    private fun prepareGroupsIf(predicate: () -> Boolean)
    {
        if(predicate.invoke())
        {
            sortedGroups.addAll(groups.map {
                val copy = it.deepCopy()
                copy.teamIds[(actualSeason + 1).toString()] = mutableListOf()
                copy
            }.sorted())
        }
    }

    suspend fun transferTeams()
    {
        getActualSeasonIf { actualSeason == 0 }
        getGroupsIf { groups.isEmpty() }
        prepareGroupsIf { sortedGroups.isEmpty() }
        getGroupsWithSortedTeamsIf { groupsWithSortedTeams.isEmpty() }

        transferBottomTeams()
        transferTopTeams()

        createMatches()

        addOtherTeamsToNextSeason()
        updateGroups()
        LeagueManager.changeSeason()
    }

    private fun addOtherTeamsToNextSeason()
    {
        val sortedResolvedTeams = resolvedTeamIds.toSortedSet()

        groups.forEach { group ->
            group.teamIds[actualSeason.toString()]?.forEach { teamId ->
                if(!sortedResolvedTeams.contains(teamId))
                    updateSortedGroups(group, teamId, actualSeason + 1)
            }
        }
    }

    private suspend fun updateGroups()
    {
        sortedGroups.forEach {
            GroupManager.setGroup(it)
        }
    }

    private suspend fun getActualSeasonIf(predicate: () -> Boolean)
    {
        if(predicate.invoke())
            actualSeason = LeagueManager.getActualSeason() ?: DateUtil.actualYear.toInt()
    }

    private suspend fun createMatches()
    {
        for(i in 0 until groupsPlayingPlayoff.lastIndex)
        {
            val betterGroup = groupsPlayingPlayoff[i]
            val worseGroup = groupsPlayingPlayoff[i+1]

            val betterTeams = groupsWithSortedTeams[betterGroup]
            val worseTeams = groupsWithSortedTeams[worseGroup]

            //has at least 4 teams or 3 teams if it's the first group
            if((betterTeams?.size ?: 0 > 2 && i == 0) || (betterTeams?.size ?: 0 > 3 ))
            {
                //has at least 4 teams or 3 teams if it's the last group
                if((worseTeams?.size ?: 0 > 2 && i + 1 == groupsPlayingPlayoff.lastIndex) || (worseTeams?.size ?: 0 > 3))
                {
                    if(betterTeams != null && worseTeams != null)
                    {
                        val betterTeam = betterTeams[betterTeams.lastIndex - 1]
                        val worseTeam = worseTeams[1]
                        resolvedTeamIds.addAll(listOf(betterTeam.id!!, worseTeam.id!!))
                        setMatches(listOf(betterTeam, worseTeam))
                    }
                }
            }
        }
    }

    private suspend fun setMatches(teams: List<Team>)
    {
        val tournament = RobinRoundTournament()
        tournament.setTeams(teams)
        tournament.createMatches(playoff).forEach {
            with(MatchManager.setMatch(it)) {
                if(this is ValidMatch) addMatchToTeams(self, teams)
            }
        }
    }

    private suspend fun addMatchToTeams(match: Match, teams: List<Team>)
    {
        val homeTeam = teams.find { it.id == match.homeId }
        val awayTeam = teams.find { it.id == match.awayId }

        homeTeam?.let { TeamManager.setTeam(it) }
        awayTeam?.let { TeamManager.setTeam(it) }
    }

    private suspend fun transferBottomTeams()
    {
        for(i in groupsPlayingPlayoff.indices)
        {
            if(i == groupsPlayingPlayoff.lastIndex) break
            val nextYear = actualSeason + 1
            val bottomTeam = groupsWithSortedTeams[groupsPlayingPlayoff[i]]?.last()
            bottomTeam?.let { team ->
                val worseGroup = groupsPlayingPlayoff[i+1]
                updateSortedGroups(worseGroup, team.id!!, nextYear)
                resolvedTeamIds.add(team.id!!)
                TeamManager.updateTeam(team.id, mapOf(TeamManager.groupId to worseGroup.id!!, TeamManager.groupName to worseGroup.name))
            }
        }
    }

    private suspend fun transferTopTeams()
    {
        for(i in groupsPlayingPlayoff.indices)
        {
            if(i == 0) continue
            val nextYear = actualSeason + 1
            val topTeam = groupsWithSortedTeams[groupsPlayingPlayoff[i]]?.first()
            topTeam?.let { team ->
                val betterGroup = groupsPlayingPlayoff[i-1]
                updateSortedGroups(betterGroup, team.id!!, nextYear)
                resolvedTeamIds.add(team.id!!)
                TeamManager.updateTeam(team.id, mapOf(TeamManager.groupId to betterGroup.id!!, TeamManager.groupName to betterGroup.name))
            }
        }
    }

    private fun updateSortedGroups(group: Group, teamId: String, nextYear: Int)
    {
        with(sortedGroups) {
            val found = this[binarySearch(group)]
            found.teamIds[nextYear.toString()]?.add(teamId)
        }
    }

    private suspend fun getGroupsWithSortedTeamsIf(predicate: () -> Boolean)
    {
        if(predicate.invoke())
        {
            groups.forEach { group ->
                group.teamIds[(actualSeason + 1).toString()] = mutableListOf()
                with(GroupManager.retrieveTeamsInGroup(group.id)) {
                    if(this is ValidTeams)
                    {
                        val rankedTeams = RankingSolver(self, actualSeason).sort()
                        groupsWithSortedTeams[group] = rankedTeams
                    }
                }
            }
        }
    }

    private suspend fun getGroupsIf(predicate: () -> Boolean)
    {
        if(predicate.invoke())
        {
            with(GroupManager.retrieveAllGroupsExceptPlayoff()) {
                if(this is ValidGroups) groups.addAll(self)
            }
        }
    }
}