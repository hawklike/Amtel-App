package cz.prague.cvut.fit.steuejan.amtelapp.fragments.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMatchActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.NoTeam
import cz.prague.cvut.fit.steuejan.amtelapp.states.TeamState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam

class MatchInputResultFragment : AbstractMatchActivityFragment()
{
    private lateinit var match: Match
    private lateinit var homeTeam: TeamState
    private lateinit var awayTeam: TeamState

    private lateinit var homeName: TextView
    private lateinit var awayName: TextView
    private lateinit var changePlace: EditText
    private lateinit var changeDate: EditText
    private lateinit var updateButton: FloatingActionButton

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
        fun newInstance(): MatchInputResultFragment = MatchInputResultFragment()
    }

    override fun getName(): String = "MatchInputResultFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.match_input_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        homeName = view.findViewById(R.id.match_input_home_name)
        awayName = view.findViewById(R.id.match_input_away_name)
        changePlace = view.findViewById(R.id.match_input_change_place)
        changeDate = view.findViewById(R.id.match_input_change_date)
        updateButton = view.findViewById(R.id.match_input_update_button)

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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        getData()
        populateFields()
    }

    private fun getData()
    {
        match = matchViewModel.match.value?.let { it } ?: Match()
        homeTeam = matchViewModel.homeTeam.value?.let { it } ?: NoTeam
        awayTeam = matchViewModel.awayTeam.value?.let { it } ?: NoTeam
    }

    private fun populateFields()
    {
        if(homeTeam is ValidTeam) homeName.text = match.home
        if(awayTeam is ValidTeam) awayName.text = match.away
    }

}