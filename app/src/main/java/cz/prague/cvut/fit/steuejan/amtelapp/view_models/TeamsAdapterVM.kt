package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroup
import kotlinx.coroutines.launch

class TeamsAdapterVM : ViewModel()
{
    fun addToGroup(team: Team, groupName: String)
    {
        viewModelScope.launch {
            if(team.group != null)
            {
                if(team.group != groupName)
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
        TeamManager.updateTeam(teamId, mapOf("group" to name))

        val group = GroupManager.findGroup(name)
        if(group is ValidGroup)
        {
            val year = DateUtil.actualYear

            val teamIds = group.self.teamIds[year]
            if(teamIds == null) group.self.teamIds[year] = mutableListOf()

            group.self.teamIds[year]?.add(teamId)
            GroupManager.addGroup(group.self)
        }
    }

    private suspend fun removeFromGroup(team: Team)
    {
        val group = GroupManager.findGroup(team.group)
        if(group is ValidGroup)
        {
            val season = group.self.teamIds[DateUtil.actualYear]
            season?.remove(team.id!!)
            if(season != null && season.isEmpty()) group.self.teamIds.remove(DateUtil.actualYear)
            GroupManager.addGroup(group.self)
        }
    }

}