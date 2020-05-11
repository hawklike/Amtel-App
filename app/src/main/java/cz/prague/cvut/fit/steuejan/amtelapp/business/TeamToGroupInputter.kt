package cz.prague.cvut.fit.steuejan.amtelapp.business

import android.widget.Toast
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.GroupRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroup
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroups

/*
This class is responsible for inputting a team into a group.
 */
class TeamToGroupInputter
{
    private var playoff = false

    fun isPlayoff(playoff: Boolean): TeamToGroupInputter
    {
        this.playoff = playoff
        return this
    }

    //adds team to a group
    suspend fun addToGroup(team: Team, groupName: String?)
    {
        if(groupName == null) return

        //team is already in a group
        if(team.groupName != null)
        {
            //remove team from current group
            removeFromGroup(team)
            //add team to a new group
            add2Group(team, groupName)
        }
        //add team to a new group
        else add2Group(team, groupName)
    }

    private suspend fun add2Group(team: Team, name: String)
    {
        //find a group
        val groups = GroupRepository.findGroups(GroupRepository.name, name)
        if(groups is ValidGroups && groups.self.isNotEmpty())
        {
            //group of the given name should be only one
            val group = groups.self.first()
            //update team in database
            TeamRepository.updateTeam(team.id, mapOf(TeamRepository.groupName to name, TeamRepository.groupId to group.id))

            team.groupName = name
            team.groupId = group.id

            //get team IDs from the group in an actual season
            val teamIds = group.teamIds[DateUtil.actualSeason]
            if(teamIds == null) group.teamIds[DateUtil.actualSeason] = mutableListOf() //create if not existing

            //convert to set and add the team
            val teamIdsSet = group.teamIds[DateUtil.actualSeason]!!.toMutableSet()
            teamIdsSet.add(team.id!!)

            //convert back to list
            group.teamIds[DateUtil.actualSeason] = teamIdsSet.toMutableList()
            //update group in database
            GroupRepository.setGroup(group)
            if(!playoff) toast(context.getString(R.string.team) + " ${team.name} " + context.getString(R.string.was_moved_to_group) + " $name" + ".", length = Toast.LENGTH_LONG)
        }
    }

    private suspend fun removeFromGroup(team: Team)
    {
        val group = GroupRepository.findGroup(team.groupId)
        if(group is ValidGroup)
        {
            //retrieve IDs of teams in the group and an actual season
            val season = group.self.teamIds[DateUtil.actualSeason]
            season?.removeAll { it == team.id }
            if(season != null && season.isEmpty())
            {
                //remove team from the group and actual season
                group.self.teamIds.remove(DateUtil.actualSeason)
            }
            //update group in database
            GroupRepository.setGroup(group.self, false)
        }
    }
}