package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group

class GroupDeleter(private val group: Group)
{
    suspend fun deleteGroup()
    {
        group.teamIds[DateUtil.actualSeason]?.forEach { teamId ->
            TeamManager.updateTeam(teamId, mapOf("groupName" to null, "groupId" to null))
        }
        GroupManager.deleteGroup(group.id)
    }
}