package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import android.widget.Toast
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroup
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroups

class TeamToGroupInputter
{
    suspend fun addToGroup(team: Team, groupName: String?)
    {
        if(groupName == null) return
        
        if(team.groupName != null)
        {
            if(team.groupName != groupName)
            {
                removeFromGroup(team)
                add2Group(team, groupName)
            }
        }
        else add2Group(team, groupName)
    }

    private suspend fun add2Group(team: Team, name: String)
    {
        val groups = GroupManager.findGroups(GroupManager.name, name)
        if(groups is ValidGroups && groups.self.isNotEmpty())
        {
            val group = groups.self.first()
            TeamManager.updateTeam(team.id, mapOf(TeamManager.groupName to name, TeamManager.groupId to group.id))
            team.groupName = name

            val teamIds = group.teamIds[DateUtil.actualSeason]
            if(teamIds == null) group.teamIds[DateUtil.actualSeason] = mutableListOf()

            group.teamIds[DateUtil.actualSeason]?.add(team.id!!)
            GroupManager.setGroup(group)
            toast(context.getString(R.string.team) + " ${team.name} " + context.getString(R.string.was_moved_to_group) + " $name" + ".", length = Toast.LENGTH_LONG)
        }
    }

    private suspend fun removeFromGroup(team: Team)
    {
        val group = GroupManager.findGroup(team.groupId)
        if(group is ValidGroup)
        {
            val season = group.self.teamIds[DateUtil.actualSeason]
            season?.removeAll { it == team.id }
            if(season != null && season.isEmpty()) group.self.teamIds.remove(DateUtil.actualSeason)
            GroupManager.setGroup(group.self)
        }
    }
}