package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
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

    private val homeManager: User? by lazy { homeTeam.users.find { it.role.toRole() == UserRole.TEAM_MANAGER } }
    private val awayManager: User? by lazy { awayTeam.users.find { it.role.toRole() == UserRole.TEAM_MANAGER } }

    private lateinit var homeName: TextView
    private lateinit var awayName: TextView
    private lateinit var score: TextView

    private lateinit var changePlace: EditText
    private lateinit var changeDate: EditText

    private lateinit var editButton: FloatingActionButton

    private var matchInfoLayout: RelativeLayout? = null
    private var progressBarLayout: FrameLayout? = null

    private lateinit var sendEmailOpponent: RelativeLayout
    private lateinit var callOpponent: RelativeLayout

    private var activityStarted: Boolean = false

    companion object
    {
        const val MATCH = "match"
        const val WEEK = "week"
        const val MATCH_RESULT_CODE = 1
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

        sendEmailOpponent = findViewById(R.id.match_arrangement_send_email)
        callOpponent = findViewById(R.id.match_arrangement_call)

        getData()
        setListeners()
    }

    override fun onResume()
    {
        super.onResume()
        activityStarted = false
    }

    override fun onDestroy()
    {
        super.onDestroy()
        progressBarLayout?.removeAllViews()
        matchInfoLayout?.removeAllViews()

        sendEmailOpponent.setOnClickListener(null)
        callOpponent.setOnClickListener(null)

        progressBarLayout = null
        matchInfoLayout = null
    }

    private fun getData()
    {
        intent.extras?.let { bundle ->
            match = bundle.getParcelable(MATCH)!!
            week = bundle.getParcelable<ValidWeek?>(WEEK)?.let { it } ?: InvalidWeek()
        }

        viewModel.setMatch(match)
        viewModel.getTeams(week)
        viewModel.teams.observe(this) {
            homeTeam = it.first
            awayTeam = it.second

            progressBarLayout?.visibility = View.GONE
            matchInfoLayout?.visibility = View.VISIBLE
            sendEmailOpponent.visibility = View.VISIBLE
            callOpponent.visibility = View.VISIBLE

            viewModel.initPlace()
            populateFields()
        }
    }

    private fun populateFields()
    {
        homeName.text = homeTeam.name
        awayName.text = awayTeam.name
        score.text = match.homeScore?.let { "$it : ${match.awayScore}" } ?: "N/A"

        changePlace.setText(match.place?.let { it } ?: homeTeam.place )

        viewModel.date.observe(this) { date ->
            date?.let { changeDate.setText(it.toMyString("dd.MM.yyyy 'v' HH:mm")) }
        }
    }

    private fun setListeners()
    {
        editButtonListener()
        changePlaceListener()
        changeDateListener()
        sendEmail()
        call()
    }

    private fun call()
    {
        callOpponent.setOnClickListener {
            awayManager?.phone?.let {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$it")
                startActivity(intent)
            } ?: toast("${awayManager?.name} ${awayManager?.surname} nemá uložené telefonní číslo.")
        }
    }

    private fun sendEmail()
    {
        sendEmailOpponent.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                type = "message/rfc822"
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(awayManager?.email ?: ""))
                putExtra(Intent.EXTRA_SUBJECT, "Zápas ${homeTeam.name}–${awayTeam.name} (skupina ${match.group})")
                putExtra(Intent.EXTRA_TEXT, "")
            }

            try { startActivity(Intent.createChooser(intent, "Poslat email" + "...")) }
            catch(ex: ActivityNotFoundException) { toast("Nemáte naistalovaný emailový klient.") }
        }
    }

    private fun editButtonListener()
    {
        editButton.setOnClickListener {
            startMatchInputResultActivity(match, getString(R.string.match_input))
        }
    }

    private fun changePlaceListener()
    {
        changePlace.setOnClickListener {
            MaterialDialog(this).show {
                title(R.string.change_place_match)
                input(hint = getString(R.string.court_address), prefill = changePlace.text)
                positiveButton(R.string.ok) {
                    val place = this.getInputField().text
                    changePlace.text = place
                    viewModel.updatePlace(place.toString())
                }
            }
        }
    }

    private fun changeDateListener()
    {
        changeDate.setOnClickListener {
            MaterialDialog(this).show {
                title(R.string.change_dateTime_match)

                val savedDate = changeDate.text?.let {
                    viewModel.setDialogDate(it)
                }

                dateTimePicker(currentDateTime = savedDate, autoFlipToTime = true, show24HoursView = true) { _, date ->
                    viewModel.updateDateTime(date)
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

        if(!activityStarted)
        {
            activityStarted = true
            startActivityForResult(intent, MATCH_RESULT_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == MATCH_RESULT_CODE && resultCode == Activity.RESULT_OK)
        {
            data?.let {
                match = it.getParcelableExtra(MATCH)
                viewModel.countTotalScore(match)
                score.text = match.homeScore?.let { "${match.homeScore} : ${match.awayScore}" } ?: "N/A"
            }
        }
    }
}