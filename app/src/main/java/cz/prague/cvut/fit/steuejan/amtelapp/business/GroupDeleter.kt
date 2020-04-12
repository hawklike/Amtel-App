package cz.prague.cvut.fit.steuejan.amtelapp.business

import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.GroupRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group

class GroupDeleter(private val group: Group)
{
    suspend fun deleteGroup(): Boolean
    {
        group.teamIds[DateUtil.actualSeason]?.forEach { teamId ->
            TeamRepository.updateTeam(teamId, mapOf("groupName" to null, "groupId" to null))
        }
        return GroupRepository.deleteGroup(group.id)
    }
}