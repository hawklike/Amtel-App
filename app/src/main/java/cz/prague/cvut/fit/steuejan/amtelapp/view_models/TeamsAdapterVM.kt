package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.LeagueManager
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
                    add2Group(team, groupName)
                }
            }
            else add2Group(team, groupName)
        }
    }

    private suspend fun add2Group(team: Team, name: String)
    {
        val groups = GroupManager.findGroups(GroupManager.name, name)
        if(groups is ValidGroups)
        {
            val group = groups.self.first()
            TeamManager.updateTeam(team.id, mapOf(TeamManager.groupName to name, TeamManager.groupId to group.id))
            val season = LeagueManager.getActualSeason()?.toString() ?: DateUtil.actualSeason

            val teamIds = group.teamIds[season]
            if(teamIds == null) group.teamIds[season] = mutableListOf()

            group.teamIds[season]?.add(team.id!!)
            GroupManager.setGroup(group)
            toast(
                context.getString(R.string.team) + " ${team.name} " + context.getString(R.string.was_moved_to_group) + " $name" + ".",
                length = Toast.LENGTH_LONG
            )
        }
    }

    private suspend fun removeFromGroup(team: Team)
    {
        val group = GroupManager.findGroup(team.groupId)
        if(group is ValidGroup)
        {
            val seasonNumber = LeagueManager.getActualSeason()?.toString() ?: DateUtil.actualSeason
            val season = group.self.teamIds[seasonNumber]
            season?.removeAll { it == team.id }
            if(season != null && season.isEmpty()) group.self.teamIds.remove(seasonNumber)
            GroupManager.setGroup(group.self)
        }
    }

}