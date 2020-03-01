package cz.prague.cvut.fit.steuejan.amtelapp.fragments.match

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
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMatchActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MatchInputResultFragmentVM

class MatchInputResultFragment : AbstractMatchActivityFragment()
{
    private val viewModel by viewModels<MatchInputResultFragmentVM>()

    private var round = 0
    private lateinit var match: Match
    private lateinit var homeTeam: Team
    private lateinit var awayTeam: Team
    private lateinit var week: WeekState

    private lateinit var homeName: TextView
    private lateinit var awayName: TextView
    private lateinit var sets: TextView

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

    companion object
    {
        private const val ROUND = "round"

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

        reportButton = view.findViewById(R.id.match_input_report_button)

        homePlayers = view.findViewById(R.id.match_input_players_home)
        awayPlayers = view.findViewById(R.id.match_input_players_away)
        sets = view.findViewById(R.id.match_input_sets)

        firstSetHome = view.findViewById(R.id.match_input_results_first_set_home)
        firstSetAway = view.findViewById(R.id.match_input_results_first_set_away)

        secondSetHome = view.findViewById(R.id.match_input_results_second_set_home)
        secondSetAway = view.findViewById(R.id.match_input_results_second_set_away)

        thirdSetHome = view.findViewById(R.id.match_input_results_third_set_home)
        thirdSetAway = view.findViewById(R.id.match_input_results_third_set_away)

        inputResult = view.findViewById(R.id.match_input_results_save_button)

        overviewLayout = view.findViewById(R.id.match_input_overview)
        resultsLayout = view.findViewById(R.id.match_input_results)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        getData()
        populateFields()
        setListeners()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        homePlayers.setOnClickListener(null)
        awayPlayers.setOnClickListener(null)
        inputResult.setOnClickListener(null)

        overviewLayout?.removeAllViews()
        resultsLayout?.removeAllViews()

        overviewLayout = null
        resultsLayout = null
    }

    private fun getData()
    {
        match = matchViewModel.match.value?.let { it } ?: Match()
        homeTeam = matchViewModel.homeTeam.value?.let { if(it is ValidTeam) it.self else Team() } ?: Team()
        awayTeam = matchViewModel.awayTeam.value?.let { if(it is ValidTeam) it.self else Team() } ?: Team()
        week = matchViewModel.week.value?.let { it } ?: InvalidWeek()
    }

    private fun populateFields()
    {
        when(AuthManager.currentUser!!.uid)
        {
            homeTeam.tmId -> {
                reportButton.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.veryLightGrey))
                reportButton.isEnabled = false
            }
            awayTeam.tmId -> {
                inputResult.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.veryLightGrey))
                inputResult.isEnabled = false
            }
        }

        homeName.text = match.home
        awayName.text = match.away
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
            val firstHome = firstSetHome.text.toString()
            val firstAway = firstSetAway.text.toString()
            val secondHome = secondSetHome.text.toString()
            val secondAway = secondSetAway.text.toString()
            val thirdHome = thirdSetHome.text.toString()
            val thirdAway = thirdSetAway.text.toString()

//            val homePlayers = homePlayers.text.trim().split(",")
//            val awayPlayers = awayPlayers.text.trim().split(",")
        }
    }

    private fun addPlayersDialog(editText: EditText, team: Team, title: String): MaterialDialog
    {
        return MaterialDialog(activity!!).show {
            title(text = title)

            val players = team.users.map { "${it.name} ${it.surname}\n${it.email}" }
            if(round == 3)
                listItemsMultiChoice(items = players) { _, _, items ->
                    editText.setText(items.joinToString(", ") { it.toString().replace("\n", " - ") })
                }
            else
                listItemsSingleChoice(items = players) { _, _, item ->
                    editText.setText(item.toString().replace("\n", " - "))
                }
            positiveButton(R.string.ok)
        }
    }


}