package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.realtime.ShowMessagesFirestoreAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager.SignedIn.HEAD_OF_LEAGUE
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager.SignedIn.HOME_MANAGER
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toCalendar
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Message
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.MessageRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.services.CountMatchScoreService
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities.MatchArrangementActivityVM
import java.util.*

class MatchArrangementActivity : AbstractBaseActivity(), ShowMessagesFirestoreAdapter.DataLoadedListener
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
            return if(currentRole == HOME_MANAGER) awayManager
            else homeManager
        }

    private lateinit var userName: String

    private lateinit var homeName: TextView
    private lateinit var awayName: TextView
    private lateinit var score: TextView

    private lateinit var changePlace: EditText
    private lateinit var changeDate: EditText

    private lateinit var defaultEndGame: TextView

    private lateinit var addToCalendar: ImageButton
    private lateinit var editButton: FloatingActionButton

    private var matchInfoLayout: RelativeLayout? = null
    private var sendMessagesLayout: RelativeLayout? = null
    private var progressBarLayout: FrameLayout? = null
    private var messagesBarLayout: FrameLayout? = null

    private lateinit var callOpponent: ImageButton
    private lateinit var sendMessage: ImageButton

    private var messagesRecyclerView: RecyclerView? = null
    private var messagesAdapter: ShowMessagesFirestoreAdapter? = null

    private var activityStarted: Boolean = false

    companion object
    {
        const val MATCH = "match"
        const val WEEK = "week"
        const val USER_NAME = "username"
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
        messagesBarLayout = findViewById(R.id.match_arrangement_messages_progressBar)

        matchInfoLayout = findViewById(R.id.match_arrangement)
        sendMessagesLayout = findViewById(R.id.match_arrangement_messages)

        callOpponent = findViewById(R.id.match_arrangement_call)
        sendMessage = findViewById(R.id.match_arrangement_messages_send)

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
        callOpponent.setOnClickListener(null)
        sendMessage.setOnClickListener(null)
        defaultEndGame.setOnClickListener(null)
        addToCalendar.setOnClickListener(null)
        homeName.setOnClickListener(null)
        awayName.setOnClickListener(null)

        messagesAdapter?.dataLoadedListener = null
        messagesRecyclerView?.adapter = null
        messagesAdapter = null
        messagesRecyclerView = null

        progressBarLayout?.removeAllViews()
        messagesBarLayout?.removeAllViews()
        matchInfoLayout?.removeAllViews()
        sendMessagesLayout?.removeAllViews()

        progressBarLayout = null
        messagesBarLayout = null
        sendMessagesLayout = null
        matchInfoLayout = null
    }

    /*
    Messages are loaded, disables progress bar.
     */
    override fun onLoaded(position: Int)
    {
        messagesRecyclerView?.layoutManager?.scrollToPosition(position)
        messagesBarLayout?.visibility = GONE
    }

    private fun getData()
    {
        intent.extras?.let { bundle ->
            match = bundle.getParcelable(MATCH)!!
            week = bundle.getParcelable<ValidWeek?>(WEEK) ?: InvalidWeek()
            userName = bundle.getString(USER_NAME, "anonym")
        }

        viewModel.setMatch(match)
        viewModel.getTeams(week)
        viewModel.teams.observe(this) {
            homeTeam = it.first
            awayTeam = it.second

            progressBarLayout?.visibility = GONE
            matchInfoLayout?.visibility = VISIBLE
            sendMessagesLayout?.visibility = VISIBLE
            messagesBarLayout?.visibility = VISIBLE

            viewModel.initPlace()
            populateFields()
        }
    }

    private fun populateFields()
    {
        homeName.text = homeTeam.name
        awayName.text = awayTeam.name
        score.text = match.homeScore?.let { "$it : ${match.awayScore}" } ?: "N/A"

        changePlace.setText(match.place ?: homeTeam.place )

        viewModel.date.observe(this) { date ->
            date?.let { changeDate.setText(it.toMyString(getString(R.string.dateTime_format))) }
        }

        //home manager may set a default end game
        if(currentRole == HOME_MANAGER || currentRole == HEAD_OF_LEAGUE)
        {
            defaultEndGame.visibility = VISIBLE
            if(match.defaultEndGameEdits <= 0) disableDefaultEndGame()
        }

        showMessages()
    }

    private fun setListeners()
    {
        editButtonListener()
        changePlaceListener()
        changeDateListener()
        sendMessage()
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
                title(text = getString(R.string.add_match_to_calendar))
                positiveButton(R.string.yes) {
                    addToCalendarIntent()
                }
                negativeButton()
            }
        }
    }

    private fun addToCalendarIntent()
    {
        //time when a match starts
        val startMillis = match.dateAndTime?.toCalendar()?.run {
            timeInMillis
        } ?: run { toast(getString(R.string.date_time_not_found)); return }

        //time when a match finishes, defaultly set to three hours after start time
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

        try { startActivity(Intent.createChooser(intent, getString(R.string.add_match) + "...")) }
        catch(ex: ActivityNotFoundException) { toast(getString(R.string.calendar_not_installed)) }
    }

    /*
    Home manager or head of league may set default end game.
     */
    private fun defaultEndGame()
    {
        defaultEndGame.setOnClickListener {
            MaterialDialog(this).show {
                title(text = getString(R.string.default_end_game))

                val msg =
                    if(match.defaultEndGameEdits == 2) getString(R.string.default_end_game_choose_winner_twoEdits)
                    else getString(R.string.default_end_game_choose_winner_oneEdit)

                message(text = msg)
                listItemsSingleChoice(items = listOf(homeTeam.name, awayTeam.name)) { _, _, text ->
                    val isHomeWinner = text == homeTeam.name
                    if(--match.defaultEndGameEdits <= 0) disableDefaultEndGame()

                    countDefaultMatchScore(viewModel.defaultEndGame(match, isHomeWinner, homeTeam, awayTeam))

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
                try { startActivity(intent) }
                catch(ex: ActivityNotFoundException) { toast(getString(R.string.phone_not_supporting_calls)) }
            } ?: toast(opponent?.name.toString() + " " + opponent?.surname + getString(R.string.phone_number_not_saved))
        }
    }

    private fun sendMessage()
    {
        val messageField = findViewById<EditText>(R.id.match_arrangement_messages_text)
        sendMessage.setOnClickListener {
            val messageText = messageField.text.toString().trim()
            messageField.text.clear()
            viewModel.sendMessage(userName, messageText, match.id)
        }
    }

    private fun showMessages()
    {
        messagesRecyclerView = findViewById(R.id.match_arrangement_messages_recyclerView)
        messagesRecyclerView?.setHasFixedSize(true)
        messagesRecyclerView?.layoutManager = LinearLayoutManager(this)

        match.id?.let {
            val query = MessageRepository.getMessages(it, true)

            val options = FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message::class.java)
                .setLifecycleOwner(this)
                .build()

            messagesAdapter = ShowMessagesFirestoreAdapter(
                options
            )
            messagesAdapter?.dataLoadedListener = this
            messagesRecyclerView?.adapter = messagesAdapter
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
            viewModel.displayWelcomeToast()
            startActivityForResult(intent, MATCH_RESULT_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == MATCH_RESULT_CODE && resultCode == Activity.RESULT_OK)
        {
            data?.let {
                //retrieve the last updated match from MatchInputResultFragment
                val lastUpdated = match.lastUpdate
                match = it.getParcelableExtra(MATCH)
                viewModel.sendEmail(lastUpdated, match, homeTeam, awayTeam)
                val result = viewModel.countTotalScore(match)
                score.text = if(result.home + result.away != 0) "${result.home} : ${result.away}" else "N/A"
            }
        }
    }

    /*
    Starts a service which resolves default end game.
     */
    private fun countDefaultMatchScore(match: Match)
    {
        val intent = Intent(this, CountMatchScoreService::class.java).apply {
            putExtra(CountMatchScoreService.HOME_TEAM, homeTeam)
            putExtra(CountMatchScoreService.AWAY_TEAM, awayTeam)
            putExtra(CountMatchScoreService.MATCH, match)
            putExtra(CountMatchScoreService.DEFAULT_LOSS, true)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun disableDefaultEndGame()
    {
        defaultEndGame.isEnabled = false
        defaultEndGame.setTextColor(App.getColor(R.color.middleLightGrey))
    }
}