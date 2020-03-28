package cz.prague.cvut.fit.steuejan.amtelapp.services

import android.app.IntentService
import android.content.Intent
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.GroupDeleter
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
           GroupDeleter(group).deleteGroup()
        }
    }
}