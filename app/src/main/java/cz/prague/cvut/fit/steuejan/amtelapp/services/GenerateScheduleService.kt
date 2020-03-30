package cz.prague.cvut.fit.steuejan.amtelapp.services

import android.app.IntentService
import android.content.Intent
import android.widget.Toast
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
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

    private var ok = true

    override fun onHandleIntent(intent: Intent)
    {
        val group = intent.getParcelableExtra<Group>(GROUP)
        val rounds = intent.getIntExtra(ROUNDS, 0)
        val regenerate = intent.getBooleanExtra(REGENERATE, false)

        val helper = ScheduleGenerator()

        runBlocking {
            ok = if(regenerate) helper.regenerateMatches(group, rounds)
            else helper.generateMatches(group, rounds)
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        if(ok) toast("Utkání byla úspěšně vytvořena.", length = Toast.LENGTH_LONG)
        else toast("Utkání se nepodařila vytvořit.", length = Toast.LENGTH_LONG)
    }
}