package cz.prague.cvut.fit.steuejan.amtelapp.services

import android.app.IntentService
import android.content.Intent
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.business.GroupDeleter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import kotlinx.coroutines.runBlocking

class GroupDeletionService : IntentService(GroupDeletionService::class.simpleName)
{
    companion object
    {
        const val GROUP = "group"
    }

    private var ok = true

    override fun onHandleIntent(intent: Intent)
    {
        val group = intent.getParcelableExtra<Group>(GROUP)

        runBlocking {
           ok = GroupDeleter(group)
               .deleteGroup()
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        if(ok) toast("Skupina byla úspěšně smazána.")
        else toast("Skupinu se nepodařilo odstranit.")
    }
}