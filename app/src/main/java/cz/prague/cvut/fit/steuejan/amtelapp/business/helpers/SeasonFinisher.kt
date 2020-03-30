package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
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
    private val groupsNotPlayingPlayoff by lazy { groups.filter { !it.playingPlayOff } }

    private val playoff = Group("playoff", context.getString(R.string.playOff), playingPlayOff = false, playOff = true, rank = Int.MAX_VALUE)

    private val resolvedTeams = mutableSetOf<Team>()

    private val year = DateUtil.actualYear

    suspend fun createPlayoff(): Boolean
            = GroupManager.addPlayoff(playoff) is ValidGroup

    suspend fun updateTeamRanks()
    {
       getGroupsIf { groups.isEmpty() }

        groups.forEach { group ->
            with(GroupManager.retrieveTeamsInGroup(group.id)) {
                if(this is ValidTeams)
                {
                    val rankedTeams = RankingSolver(self, year.toInt()).sort()
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

    suspend fun transferTeams()
    {
        getGroupsIf { groups.isEmpty() }
        getGroupsWithSortedTeamsIf { groupsWithSortedTeams.isEmpty() }

        transferBottomTeams()
        transferTopTeams()

        createMatches()
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
                        resolvedTeams.addAll(listOf(betterTeam, worseTeam))
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
            val nextYear = year.toInt() + 1
            val bottomTeam = groupsWithSortedTeams[groupsPlayingPlayoff[i]]?.last()
            bottomTeam?.let {
                val worseGroup = groupsPlayingPlayoff[i+1].deepCopy()
                worseGroup.teamIds[nextYear.toString()] = mutableListOf(it.id!!)
                resolvedTeams.add(it)
                TeamManager.updateTeam(it.id, mapOf("groupId" to worseGroup.id!!, "groupName" to worseGroup.name))
                GroupManager.setGroup(worseGroup)
            }
        }
    }

    private suspend fun transferTopTeams()
    {
        for(i in groupsPlayingPlayoff.indices)
        {
            if(i == 0) continue
            val nextYear = year.toInt() + 1
            val topTeam = groupsWithSortedTeams[groupsPlayingPlayoff[i]]?.first()
            topTeam?.let {
                val betterGroup = groupsPlayingPlayoff[i-1].deepCopy()
                betterGroup.teamIds[nextYear.toString()] = mutableListOf(it.id!!)
                resolvedTeams.add(it)
                TeamManager.updateTeam(it.id, mapOf("groupId" to betterGroup.id!!, "groupName" to betterGroup.name))
                GroupManager.setGroup(betterGroup)
            }
        }
    }

    private suspend fun getGroupsWithSortedTeamsIf(predicate: () -> Boolean)
    {
        if(predicate.invoke())
        {
            groups.forEach { group ->
                with(GroupManager.retrieveTeamsInGroup(group.id)) {
                    if(this is ValidTeams)
                    {
                        val rankedTeams = RankingSolver(self, year.toInt()).sort()
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