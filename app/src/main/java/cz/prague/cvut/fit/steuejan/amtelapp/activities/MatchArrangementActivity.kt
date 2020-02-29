package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MatchArrangementActivityVM

class MatchArrangementActivity : AbstractBaseActivity()
{
    private val viewModel by viewModels<MatchArrangementActivityVM>()

    private lateinit var match: Match
    private lateinit var week: WeekState

    private lateinit var homeTeam: Team
    private lateinit var awayTeam: Team

    private lateinit var homeName: TextView
    private lateinit var awayName: TextView
    private lateinit var score: TextView

    private lateinit var changePlace: EditText
    private lateinit var changeDate: EditText

    private lateinit var editButton: FloatingActionButton

    private var matchInfoLayout: RelativeLayout? = null
    private var progressBarLayout: FrameLayout? = null

    companion object
    {
        const val MATCH = "match"
        const val WEEK = "week"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.match_arrangement)
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.match_arrangement))
        setArrowBack()

        homeName = findViewById(R.id.match_arrangement_home)
        awayName = findViewById(R.id.match_arrangement_away)
        score = findViewById(R.id.match_arrangement_score)

        changePlace = findViewById(R.id.match_arrangement_change_place)
        changeDate = findViewById(R.id.match_arrangement_change_date)

        editButton = findViewById(R.id.match_arrangement_edit_button)
        progressBarLayout = findViewById(R.id.match_arrangement_progressBar)
        matchInfoLayout = findViewById(R.id.match_arrangement)

        getData()
        setListeners()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        progressBarLayout?.removeAllViews()
        matchInfoLayout?.removeAllViews()

        progressBarLayout = null
        matchInfoLayout = null
    }

    private fun getData()
    {
        intent.extras?.let { bundle ->
            match = bundle.getParcelable(MATCH)!!
            week = bundle.getParcelable<ValidWeek?>(WEEK)?.let { it } ?: InvalidWeek()
        }

        viewModel.getTeams(match)
        viewModel.teams.observe(this) {
            homeTeam = it.first
            awayTeam = it.second

            progressBarLayout?.visibility = View.GONE
            matchInfoLayout?.visibility = View.VISIBLE

            populateFields()
        }
    }

    private fun populateFields()
    {
        homeName.text = homeTeam.name
        awayName.text = awayTeam.name
        score.text = match.homeScore?.let { "$it : ${match.awayScore}" } ?: "N/A"

        changePlace.setText(homeTeam.place)

        viewModel.findBestDate(homeTeam, awayTeam, week)
        viewModel.date.observe(this) { date ->
            date?.let { changeDate.setText(it.toMyString()) }
        }
    }

    private fun setListeners()
    {
        editButton.setOnClickListener {
            startMatchInputResultActivity(match, getString(R.string.match_input))
        }

        changeDate.setOnClickListener {
            MaterialDialog(this).show {
                val savedDate = changeDate.text?.let {
                    viewModel.setDialogDate(it)
                }

                datePicker(currentDate = savedDate) { _, date ->
                    val dateText = date.toMyString()
                    changeDate.setText(dateText)
                }
            }
        }
    }

    private fun startMatchInputResultActivity(match: Match, title: String)
    {
         val intent = Intent(this, MatchMenuActivity::class.java).apply {
            putExtra(MatchMenuActivity.MATCH, match)
            putExtra(MatchMenuActivity.WEEK, if(week is ValidWeek) week as ValidWeek else null)
            putExtra(MatchMenuActivity.TITLE, title)
            putExtra(MatchMenuActivity.HOME_TEAM, homeTeam)
            putExtra(MatchMenuActivity.AWAY_TEAM, awayTeam)
        }
        startActivity(intent)
    }
}