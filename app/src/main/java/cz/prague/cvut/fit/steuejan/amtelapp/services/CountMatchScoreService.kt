package cz.prague.cvut.fit.steuejan.amtelapp.services

import android.app.IntentService
import android.content.Intent
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.MatchScoreCounter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import kotlinx.coroutines.runBlocking

class CountMatchScoreService : IntentService(CountMatchScoreService::class.simpleName)
{
    companion object
    {
        const val HOME_TEAM = "home"
        const val AWAY_TEAM = "away"
        const val MATCH = "match"
        const val DEFAULT_LOSS = "isDefaultLoss"
    }

    override fun onHandleIntent(intent: Intent)
    {
        val homeTeam = intent.getParcelableExtra<Team>(HOME_TEAM)
        val awayTeam = intent.getParcelableExtra<Team>(AWAY_TEAM)
        val match = intent.getParcelableExtra<Match>(MATCH)
        val defaultLoss = intent.getBooleanExtra(DEFAULT_LOSS, false)

        runBlocking {
            MatchScoreCounter(match, homeTeam, awayTeam)
                .withDefaultLoss(defaultLoss)
                .countTotalScore()
        }
    }
}