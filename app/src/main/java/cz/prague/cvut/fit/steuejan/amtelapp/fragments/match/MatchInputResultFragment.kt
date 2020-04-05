package cz.prague.cvut.fit.steuejan.amtelapp.fragments.match

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMatchActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.services.CountMatchScoreService
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidSet
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MatchInputResultFragmentVM

class MatchInputResultFragment : AbstractMatchActivityFragment()
{
    private val viewModel by viewModels<MatchInputResultFragmentVM>()

    private var round = 0
    private var isHeadOfLeague = false
    private var isReport = false

    private lateinit var match: Match
    private lateinit var userId: String

    private lateinit var homeTeam: Team
    private lateinit var awayTeam: Team
    private lateinit var week: WeekState

    private lateinit var homeName: TextView
    private lateinit var awayName: TextView

    private lateinit var sets: TextView
    private lateinit var games: TextView

    private lateinit var reportButton: FloatingActionButton

    private lateinit var homePlayers: EditText
    private lateinit var awayPlayers: EditText

    private lateinit var firstSetHome: EditText
    private lateinit var firstSetAway: EditText

    private lateinit var secondSetHome: EditText
    private lateinit var secondSetAway: EditText

    private lateinit var thirdSetHome: EditText
    private lateinit var thirdSetAway: EditText

    private lateinit var inputResult: Button

    private var overviewLayout: RelativeLayout? = null
    private var resultsLayout: RelativeLayout? = null

    private val dialog by lazy {
        MaterialDialog(activity!!).customView(R.layout.progress_layout)
    }

    companion object
    {
        private const val ROUND = "round"
        const val EM_DASH = "\u001B–\u001B"
        const val COMMA = ",\u001B"

        fun newInstance(round: Int): MatchInputResultFragment
        {
            val fragment = MatchInputResultFragment()
            fragment.arguments = Bundle().apply {
                putInt(ROUND, round)
            }
            return fragment
        }
    }

    override fun getName(): String = "MatchInputResultFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        arguments?.getInt(ROUND)?.let { round = it }
        return inflater.inflate(R.layout.match_input_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        homeName = view.findViewById(R.id.match_input_home_name)
        awayName = view.findViewById(R.id.match_input_away_name)
        sets = view.findViewById(R.id.match_input_sets)
        games = view.findViewById(R.id.match_input_gems)

        reportButton = view.findViewById(R.id.match_input_report_button)

        homePlayers = view.findViewById(R.id.match_input_players_home)
        awayPlayers = view.findViewById(R.id.match_input_players_away)

        firstSetHome = view.findViewById(R.id.match_input_results_first_set_home)
        firstSetAway = view.findViewById(R.id.match_input_results_first_set_away)

        secondSetHome = view.findViewById(R.id.match_input_results_second_set_home)
        secondSetAway = view.findViewById(R.id.match_input_results_second_set_away)

        thirdSetHome = view.findViewById(R.id.match_input_results_third_set_home)
        thirdSetAway = view.findViewById(R.id.match_input_results_third_set_away)

        inputResult = view.findViewById(R.id.match_input_results_save_button)

        overviewLayout = view.findViewById(R.id.match_input_overview)
        resultsLayout = view.findViewById(R.id.match_input_results)

        inputResult.text = "Zapsat $round. zápas"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        getData()
        viewModel.round = round
        viewModel.setMatch(match)
        populateFields()
        setListeners()
        setObservers()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        dialog.dismiss()

        homePlayers.setOnClickListener(null)
        awayPlayers.setOnClickListener(null)
        inputResult.setOnClickListener(null)
        reportButton.setOnClickListener(null)

        overviewLayout?.removeAllViews()
        resultsLayout?.removeAllViews()

        overviewLayout = null
        resultsLayout = null
    }

    private fun getData()
    {
        userId = AuthManager.currentUser!!.uid
        match = matchViewModel.match.value?.let { it } ?: Match()
        homeTeam = matchViewModel.homeTeam.value?.let { if(it is ValidTeam) it.self else Team() } ?: Team()
        awayTeam = matchViewModel.awayTeam.value?.let { if(it is ValidTeam) it.self else Team() } ?: Team()
        week = matchViewModel.week.value?.let { it } ?: InvalidWeek()
    }

    private fun populateFields()
    {
        prepareLayout()
        val round = match.rounds[round - 1]

        homeName.text = match.home
        awayName.text = match.away
        sets.text = MatchManager.getResults(round).sets
        games.text = MatchManager.getResults(round).games

        firstSetHome.setText(round.homeGemsSet1?.toString())
        firstSetAway.setText(round.awayGemsSet1?.toString())

        secondSetHome.setText(round.homeGemsSet2?.toString())
        secondSetAway.setText(round.awayGemsSet2?.toString())

        thirdSetHome.setText(round.homeGemsSet3?.toString())
        thirdSetAway.setText(round.awayGemsSet3?.toString())

        homePlayers.setText(round.homePlayers.joinToString("$COMMA ") { "${it.name} ${it.surname} $EM_DASH ${it.email}" })
        awayPlayers.setText(round.awayPlayers.joinToString("$COMMA ") { "${it.name} ${it.surname} $EM_DASH ${it.email}" })
    }

    private fun prepareLayout()
    {
        when(userId)
        {
            homeTeam.tmId -> {
                reportButton.visibility = View.INVISIBLE
                reportButton.isEnabled = false
                disableInputButtonIf { match.edits[round.toString()] == 0 }
            }

            awayTeam.tmId -> {
                isReport = true
                matchViewModel.isReport(true)
                inputResult.visibility = View.GONE
            }

            else ->
            {
                isHeadOfLeague = true
                reportButton.visibility = View.INVISIBLE
                reportButton.isEnabled = false
            }
        }
    }

    private fun setListeners()
    {
        homePlayers.setOnClickListener {
            addPlayersDialog(it as EditText, homeTeam, getString(R.string.choose_player_home_team))
        }

        awayPlayers.setOnClickListener {
            addPlayersDialog(it as EditText, awayTeam, getString(R.string.choose_player_away_team))
        }

        inputResult.setOnClickListener {
           getInputAndConfirm()
        }

        reportButton.setOnClickListener {
            getInputAndConfirm()
        }
    }

    private fun getInputAndConfirm()
    {
        val firstHome = firstSetHome.text.toString()
        val firstAway = firstSetAway.text.toString()
        val secondHome = secondSetHome.text.toString()
        val secondAway = secondSetAway.text.toString()
        val thirdHome = thirdSetHome.text.toString()
        val thirdAway = thirdSetAway.text.toString()

        val homePlayersText = homePlayers.text
        val awayPlayersText = awayPlayers.text

        deleteErrors()

        viewModel.confirmInput(
            firstHome,
            firstAway,
            secondHome,
            secondAway,
            thirdHome,
            thirdAway,
            homePlayersText,
            awayPlayersText,
            match.groupName == getString(R.string.fifty_plus_group)
        )
    }

    private fun setObservers()
    {
        handleErrors()
        handleDialogs()
        isMatchAdded()
        isReported()
    }

    private fun isReported()
    {
        viewModel.isReported.observe(viewLifecycleOwner) { match ->
            val result = MatchManager.getResults(match.rounds[round - 1])
            viewModel.sendEmail(homeTeam, awayTeam, result.sets, result.games, userId)
            toast("Výsledek byl odeslán k posouzení.")
        }
    }

    private fun isMatchAdded()
    {
        viewModel.matchAdded.observe(viewLifecycleOwner) {
            countMatchScore()
            dialog.dismiss()
            val result = MatchManager.getResults(match.rounds[round - 1])
            sets.text = result.sets
            games.text = result.games
            disableInputButtonIf { !isHeadOfLeague && match.edits[round.toString()] == 0 }
            viewModel.sendEmail(homeTeam, awayTeam, sets.text, games.text, userId)
            matchViewModel.setPage(round)
        }
    }

    private fun countMatchScore()
    {
        val serviceIntent = Intent(activity!!, CountMatchScoreService::class.java).apply {
            putExtra(CountMatchScoreService.HOME_TEAM, homeTeam)
            putExtra(CountMatchScoreService.AWAY_TEAM, awayTeam)
            putExtra(CountMatchScoreService.MATCH, match)
        }
        activity!!.startService(serviceIntent)
    }

    private fun handleDialogs()
    {
        viewModel.isInputOk.observe(viewLifecycleOwner) { isOk ->
            if(isOk)
            {
                when(isReport)
                {
                    true-> {
                        displayConfirmationDialog(
                            "Chcete podat námitku?",
                            "Vámi zapsané údaje budou poslány vedoucímu soutěže k posouzení.",
                            "Podat") {
                            viewModel.inputResult(homeTeam.users, awayTeam.users, isHeadOfLeague, isReport = true) }
                    }

                    false -> {
                        val title = if(!isHeadOfLeague && match.edits[round.toString()] == 1)
                            getString(R.string.match_input_confirmation_last_attempt_text)
                        else getString(R.string.match_input_confirmation_text)

                        displayConfirmationDialog(
                            "Zapsat výsledek?",
                            title) {
                            viewModel.inputResult(homeTeam.users, awayTeam.users, isHeadOfLeague)
                            dialog.show()
                        }
                    }
                }
            }
        }

        viewModel.isTie.observe(viewLifecycleOwner) { isTie ->
            if(isTie)
            {
                //not working and don't know why
                dialog.dismiss()
                displayConfirmationDialog(
                    "Zapsat výsledek?",
                    getString(R.string.match_tie_warning)) {
                    viewModel.inputResult(
                        homeTeam.users,
                        awayTeam.users,
                        isHeadOfLeague,
                        ignoreTie = true,
                        isReport = isReport
                    )
                    dialog.show()
                }
            }
        }
    }

    private fun handleErrors()
    {
        viewModel.firstHome.observe(viewLifecycleOwner) {
            if(it is InvalidSet) firstSetHome.error = it.errorMessage
            dialog.dismiss()
        }

        viewModel.secondHome.observe(viewLifecycleOwner) {
            if(it is InvalidSet) secondSetHome.error = it.errorMessage
            dialog.dismiss()
        }

        viewModel.thirdHome.observe(viewLifecycleOwner) {
            if(it is InvalidSet) thirdSetHome.error = it.errorMessage
            dialog.dismiss()
        }

        viewModel.firstAway.observe(viewLifecycleOwner) {
            if(it is InvalidSet) firstSetAway.error = it.errorMessage
            dialog.dismiss()
        }

        viewModel.secondAway.observe(viewLifecycleOwner) {
            if(it is InvalidSet) secondSetAway.error = it.errorMessage
            dialog.dismiss()
        }

        viewModel.thirdAway.observe(viewLifecycleOwner) {
            if(it is InvalidSet) thirdSetAway.error = it.errorMessage
            dialog.dismiss()
        }

        viewModel.homePlayers.observe(viewLifecycleOwner) { isOk ->
            if(!isOk) homePlayers.error = getString(R.string.empty_players_error)
            dialog.dismiss()
        }

        viewModel.awayPlayers.observe(viewLifecycleOwner) { isOk ->
            if(!isOk) awayPlayers.error = getString(R.string.empty_players_error)
            dialog.dismiss()
        }

    }

    private fun displayConfirmationDialog(title: String, message: String, button: String = "Zapsat", callback: () -> Unit)
    {
        MaterialDialog(activity!!)
            .title(text = title)
            .message(text = message)
            .show {
                positiveButton(text = button) { callback.invoke() }
                negativeButton()
            }
    }

    /*
     *  If there is a group named '50+', two matches are doubles and one is single. Otherwise
     *  two matches are singles and one is double. According to a round number and a group name,
     *  a user may input only one or more players who played in the particular match.
     */
    private fun addPlayersDialog(editText: EditText, team: Team, title: String): MaterialDialog
    {
        return MaterialDialog(activity!!).show {
            title(text = title)
            val players = team.users.map { "${it.name} ${it.surname}\n${it.email}" }

            if(round == 3) listItemMultiChoice(this, players, editText)
            else
            {
                if(round == 2 && match.groupName == getString(R.string.fifty_plus_group)) listItemMultiChoice(this, players, editText)
                else listItemSingleChoice(this, players, editText)
            }
            positiveButton(R.string.ok)
        }
    }


    //output looks like this: <<name>> - <<email>>
    private fun listItemSingleChoice(dialog: MaterialDialog, players: List<String>, editText: EditText): MaterialDialog
    {
        return dialog.listItemsSingleChoice(items = players) { _, _, item ->
            editText.setText(item.toString().replace("\n", " $EM_DASH "))
        }
    }


     //output looks like this: <<name>> - <<email>>, <<name>> - <<email>>
    private fun listItemMultiChoice(dialog: MaterialDialog, players: List<String>, editText: EditText): MaterialDialog
    {
        return dialog.listItemsMultiChoice(items = players) { _, _, items ->
            editText.setText(items.joinToString("$COMMA ") { it.toString().replace("\n", " $EM_DASH ") })
        }
    }

    private fun deleteErrors()
    {
        firstSetHome.error = null
        firstSetAway.error = null
        secondSetHome.error = null
        secondSetAway.error = null
        thirdSetHome.error = null
        thirdSetAway.error = null
        homePlayers.error = null
        awayPlayers.error = null
    }

    private fun disableInputButtonIf(predicate: () -> Boolean)
    {
        if(predicate())
        {
            inputResult.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.middleLightGrey))
            inputResult.isEnabled = false
            inputResult.elevation = 0.0F
            homePlayers.isEnabled = false
            awayPlayers.isEnabled = false
            firstSetHome.isEnabled = false
            firstSetAway.isEnabled = false
            secondSetHome.isEnabled = false
            secondSetAway.isEnabled = false
            thirdSetHome.isEnabled = false
            thirdSetAway.isEnabled = false
        }
    }


}
