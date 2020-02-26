package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState

class MatchArrangementActivity : AbstractBaseActivity()
{
    private lateinit var match: Match
    private lateinit var week: WeekState

    companion object
    {
        const val MATCH = "match"
        const val WEEK = "week"
    }

    private fun getData()
    {
        intent.extras?.let { bundle ->
            match = bundle.getParcelable(MATCH)!!
            week = bundle.getParcelable<ValidWeek?>(WEEK)?.let { it } ?: InvalidWeek()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.match_arrangement)
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.match_arrangement))
        setArrowBack()
        getData()
    }
}