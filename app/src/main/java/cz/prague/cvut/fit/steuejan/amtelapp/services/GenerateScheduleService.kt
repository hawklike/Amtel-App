package cz.prague.cvut.fit.steuejan.amtelapp.services

import android.app.IntentService
import android.content.Intent
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.ScheduleGenerator
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import kotlinx.coroutines.runBlocking

class GenerateScheduleService : IntentService(GenerateScheduleService::class.simpleName)
{
    companion object
    {
        const val GROUP = "group"
        const val ROUNDS = "rounds"
        const val REGENERATE = "regenerate"
    }

    override fun onHandleIntent(intent: Intent)
    {
        val group = intent.getParcelableExtra<Group>(GROUP)
        val rounds = intent.getIntExtra(ROUNDS, 0)
        val regenerate = intent.getBooleanExtra(REGENERATE, false)

        val helper = ScheduleGenerator()

        runBlocking {
            if(regenerate) helper.regenerateMatches(group, rounds)
            else helper.generateMatches(group, rounds)
        }
    }
}