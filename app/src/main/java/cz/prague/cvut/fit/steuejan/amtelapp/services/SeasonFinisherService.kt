package cz.prague.cvut.fit.steuejan.amtelapp.services

import android.app.IntentService
import android.content.Intent
import android.widget.Toast
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SeasonFinisher
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import kotlinx.coroutines.runBlocking

class SeasonFinisherService : IntentService(SeasonFinisherService::class.simpleName)
{
    private var ok = true

    companion object
    {
        const val GROUPS_EXCEPT_PLAYOFF = "groups"
    }

    override fun onHandleIntent(intent: Intent)
    {
        val groups = intent.getParcelableArrayListExtra<Group>(GROUPS_EXCEPT_PLAYOFF)

        val helper = SeasonFinisher(groups)

        runBlocking {
            if(!helper.createPlayoff())
            {
                ok = false
                return@runBlocking
            }
            helper.updateTeamRanks()
            helper.transferTeams()
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        if(ok) toast("Baráž byla úspěšně vygenerována.", length = Toast.LENGTH_LONG)
        else toast("Baráž se nepodařilo vygenerovat.", length = Toast.LENGTH_LONG)
    }

}