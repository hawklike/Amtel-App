package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toCalendar
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.services.CountMatchScoreService
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MatchArrangementActivityVM
import java.util.*

class MatchArrangementActivity : AbstractBaseActivity()
{
    private val viewModel by viewModels<MatchArrangementActivityVM>()

    private lateinit var match: Match
    private lateinit var week: WeekState

    private lateinit var homeTeam: Team
    private lateinit var awayTeam: Team

    private val homeManager: User? by lazy { homeTeam.users.find { it.role.toRole() == UserRole.TEAM_MANAGER } }
    private val awayManager: User? by lazy { awayTeam.users.find { it.role.toRole() == UserRole.TEAM_MANAGER } }

    private val currentRole: AuthManager.SignedIn by lazy { AuthManager.getCurrentRole(homeManager?.id, awayManager?.id) }

    private val opponent: User?
        get()
        {
            return if(currentRole == AuthManager.SignedIn.HOME_MANAGER) awayManager
            else homeManager
        }

    private lateinit var homeName: TextView
    private lateinit var awayName: TextView
    private lateinit var score: TextView

    private lateinit var changePlace: EditText
    private lateinit var changeDate: EditText

    private lateinit var defaultEndGame: TextView

    private lateinit var addToCalendar: ImageButton
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

        defaultEndGame = findViewById(R.id.match_arrangement_default)

        addToCalendar = findViewById(R.id.match_arrangement_calendar)
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
        sendEmailOpponent.setOnClickListener(null)
        callOpponent.setOnClickListener(null)
        defaultEndGame.setOnClickListener(null)
        addToCalendar.setOnClickListener(null)
        homeName.setOnClickListener(null)
        awayName.setOnLongClickListener(null)

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

        if(currentRole == AuthManager.SignedIn.HOME_MANAGER)
        {
            defaultEndGame.visibility = View.VISIBLE
            if(match.defaultEndGameEdits <= 0) disableDefaultEndGame()
        }
    }

    private fun setListeners()
    {
        editButtonListener()
        changePlaceListener()
        changeDateListener()
        sendEmail()
        call()
        defaultEndGame()
        addToCalendar()
        startTeamInfoActivity()
    }

    private fun startTeamInfoActivity()
    {
        homeName.setOnClickListener {
            startTeamInfoActivity(homeTeam)
        }

        awayName.setOnClickListener {
            startTeamInfoActivity(awayTeam)
        }
    }

    private fun startTeamInfoActivity(team: Team)
    {
        val intent = Intent(this, TeamInfoActivity::class.java).apply {
            putExtra(TeamInfoActivity.TEAM, team)
        }
        startActivity(intent)
    }

    private fun addToCalendar()
    {
        addToCalendar.setOnClickListener {
            MaterialDialog(this).show {
                title(text = "Přidat utkání do kalendáře?")
                positiveButton(R.string.yes) {
                    addToCalendarIntent()
                }
                negativeButton()
            }
        }
    }

    private fun addToCalendarIntent()
    {
        val startMillis = match.dateAndTime?.toCalendar()?.run {
            timeInMillis
        } ?: run { toast("Nebyl nalezen datum a čas utkání."); return }

        val endMillis = match.dateAndTime?.toCalendar()?.run {
            this.add(Calendar.HOUR_OF_DAY, 3)
            timeInMillis
        }

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.TITLE, "Utkání ${homeTeam.name}–${awayTeam.name} (${match.round}. kolo) [AMTEL Opava]")
            putExtra(CalendarContract.Events.DESCRIPTION, "Utkání ${homeTeam.name}–${awayTeam.name} v ${match.round}. kole soutěže AMTEL Opava. Oponent: ${opponent?.name ?: ""} ${opponent?.surname ?: ""}.")
            putExtra(CalendarContract.Events.EVENT_LOCATION, match.place)
        }

        startActivity(Intent.createChooser(intent, "Přidat utkání" + "..."))
    }

    private fun defaultEndGame()
    {
        defaultEndGame.setOnClickListener {
            MaterialDialog(this).show {
                title(text = "Kontumace")

                val msg =
                    if(match.defaultEndGameEdits == 2) "Zvolte prosím vítěze. Výsledek budete moct jednou opravit."
                    else "Zvolte prosím vítěze. Máte poslední možnost opravy!"

                message(text = msg)
                listItemsSingleChoice(items = listOf(homeTeam.name, awayTeam.name)) { _, _, text ->
                    val isHomeWinner = text == homeTeam.name
                    if(--match.defaultEndGameEdits <= 0) disableDefaultEndGame()

                    countMatchScore(viewModel.defaultEndGame(match, isHomeWinner, homeTeam, awayTeam), true)

                    score.text = if(isHomeWinner) "3 : 0" else "0 : 3"
                    toast("Tým ${if(isHomeWinner) homeTeam.name else awayTeam.name} kontumačně vyhrál.")
                }
                positiveButton()
                negativeButton()
            }
        }
    }

    private fun call()
    {
        callOpponent.setOnClickListener {
            opponent?.phone?.let {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$it")
                startActivity(intent)
            } ?: toast("${opponent?.name} ${opponent?.surname} nemá uložené telefonní číslo.")
        }
    }

    private fun sendEmail()
    {
        sendEmailOpponent.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                type = "message/rfc822"
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(opponent?.email ?: ""))
                putExtra(Intent.EXTRA_SUBJECT, "Zápas ${homeTeam.name}–${awayTeam.name} (skupina ${match.groupName})")
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
                    match.place = place.toString()
                }
                negativeButton()
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
                    match.dateAndTime = date.time
                    viewModel.updateDateTime(date)
                }
            }
        }
    }

    private fun startMatchInputResultActivity(match: Match, title: String)
    {
         val intent = Intent(this, MatchViewPagerActivity::class.java).apply {
            putExtra(MatchViewPagerActivity.MATCH, match)
            putExtra(MatchViewPagerActivity.WEEK, if(week is ValidWeek) week as ValidWeek else null)
            putExtra(MatchViewPagerActivity.TITLE, title)
            putExtra(MatchViewPagerActivity.HOME_TEAM, homeTeam)
            putExtra(MatchViewPagerActivity.AWAY_TEAM, awayTeam)
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
                val result = viewModel.countTotalScore(match)
                score.text = if(result.home + result.away != 0) "${result.home} : ${result.away}" else "N/A"
            }
        }
    }

    private fun countMatchScore(match: Match, isDefaultLoss: Boolean)
    {
        val serviceIntent = Intent(this, CountMatchScoreService::class.java).apply {
            putExtra(CountMatchScoreService.HOME_TEAM, homeTeam)
            putExtra(CountMatchScoreService.AWAY_TEAM, awayTeam)
            putExtra(CountMatchScoreService.MATCH, match)
            putExtra(CountMatchScoreService.DEFAULT_LOSS, isDefaultLoss)
        }
        startService(serviceIntent)
    }

    private fun disableDefaultEndGame()
    {
        defaultEndGame.isEnabled = false
        defaultEndGame.setTextColor(App.getColor(R.color.middleLightGrey))
    }
}