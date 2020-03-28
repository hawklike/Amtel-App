package cz.prague.cvut.fit.steuejan.amtelapp.services

import android.app.IntentService
import android.content.Intent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import kotlinx.coroutines.runBlocking

class GroupDeletionService : IntentService(GroupDeletionService::class.simpleName)
{
    companion object
    {
        const val GROUP = "group"
    }

    override fun onHandleIntent(intent: Intent)
    {
        val group = intent.getParcelableExtra<Group>(GROUP)

        runBlocking {
            group.teamIds[DateUtil.actualYear]?.forEach { teamId ->
                TeamManager.updateTeam(teamId, mapOf("groupName" to null, "groupId" to null))
            }
            GroupManager.deleteGroup(group.id)
        }
    }
}