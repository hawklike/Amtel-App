package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState

class MatchArrangementActivity : AbstractBaseActivity()
{
    private lateinit var match: Match
    private lateinit var week: WeekState

    private lateinit var button: Button

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

        button = findViewById(R.id.match_arrangement_button)

        setListeners()
    }

    private fun setListeners()
    {
        button.setOnClickListener {
            startMatchInputResultActivity(match, getString(R.string.match_input))
        }
    }

    private fun startMatchInputResultActivity(match: Match, title: String)
    {
        val intent = Intent(this, MatchMenuActivity::class.java).apply {
            putExtra(MatchMenuActivity.MATCH, match)
            putExtra(MatchMenuActivity.WEEK, if(week is ValidWeek) week as ValidWeek else null)
            putExtra(MatchMenuActivity.TITLE, title)
        }
        startActivity(intent)
    }
}