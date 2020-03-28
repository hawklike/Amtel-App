package cz.prague.cvut.fit.steuejan.amtelapp.services

import android.app.IntentService
import android.content.Intent
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SeasonFinisher
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import kotlinx.coroutines.runBlocking

class SeasonFinisherService : IntentService(SeasonFinisherService::class.simpleName)
{
    companion object
    {
        const val GROUPS_EXCEPT_PLAYOFF = "groups"
    }

    override fun onHandleIntent(intent: Intent)
    {
        val groups = intent.getParcelableArrayListExtra<Group>(GROUPS_EXCEPT_PLAYOFF)

        val helper = SeasonFinisher(groups)

        runBlocking {
            if(!helper.createPlayoff()) return@runBlocking
            helper.updateTeamRanks()
        }
    }

}