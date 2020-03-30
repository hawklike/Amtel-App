package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroup
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroups
import kotlinx.coroutines.launch

class TeamsAdapterVM : ViewModel()
{
    fun addToGroup(team: Team, groupName: String)
    {
        viewModelScope.launch {
            if(team.groupName != null)
            {
                if(team.groupName != groupName)
                {
                    removeFromGroup(team)
                    addToGroup(team.id!!, groupName)
                }
            }
            else addToGroup(team.id!!, groupName)
        }
    }

    private suspend fun addToGroup(teamId: String, name: String)
    {
        val groups = GroupManager.findGroups("name", name)
        if(groups is ValidGroups)
        {
            val group = groups.self.first()
            TeamManager.updateTeam(teamId, mapOf("groupName" to name, "groupId" to group.id))
            val year = DateUtil.actualYear

            val teamIds = group.teamIds[year]
            if(teamIds == null) group.teamIds[year] = mutableListOf()

            group.teamIds[year]?.add(teamId)
            GroupManager.setGroup(group)
        }
    }

    private suspend fun removeFromGroup(team: Team)
    {
        val group = GroupManager.findGroup(team.groupId)
        if(group is ValidGroup)
        {
            val season = group.self.teamIds[DateUtil.actualYear]
            season?.removeAll { it == team.id }
            if(season != null && season.isEmpty()) group.self.teamIds.remove(DateUtil.actualYear)
            GroupManager.setGroup(group.self)
        }
    }

}