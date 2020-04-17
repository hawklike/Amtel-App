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
    private lateinit var group: Group

    override fun onHandleIntent(intent: Intent)
    {
        group = intent.getParcelableExtra(GROUP)

        runBlocking {
           ok = GroupDeleter(group)
               .deleteGroup()
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        if(ok) toast("Skupina ${group.name} byla úspěšně smazána.")
        else toast("Skupinu ${group.name} se nepodařilo odstranit.")
    }
}