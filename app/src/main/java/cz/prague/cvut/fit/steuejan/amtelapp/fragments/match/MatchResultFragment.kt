package cz.prague.cvut.fit.steuejan.amtelapp.fragments.match

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.MatchDiscussionActivity
import cz.prague.cvut.fit.steuejan.amtelapp.activities.PlayerInfoActivity
import cz.prague.cvut.fit.steuejan.amtelapp.activities.TeamInfoActivity
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal.ShowPlayersAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.MatchRepository
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.StringUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMatchActivityFragment

class MatchResultFragment : AbstractMatchActivityFragment()
{
    private var roundNumber = 0

    private lateinit var match: Match
    private lateinit var round: Round

    private lateinit var homeName: TextView
    private lateinit var awayName: TextView

    private lateinit var group: TextView

    private lateinit var homeResult: Button
    private lateinit var awayResult: Button

    private lateinit var sets: TextView
    private lateinit var games: TextView

    private lateinit var place: TextView
    private lateinit var date: TextView

    private lateinit var discussion: Button

    private var homeCard: RelativeLayout? = null
    private var awayCard: RelativeLayout? = null

    private lateinit var cardHomeName: TextView
    private lateinit var cardAwayName: TextView

    private var recyclerView: RecyclerView? = null
    private var adapter: ShowPlayersAdapter? = null

    companion object
    {
        private const val ROUND = "round"

        fun newInstance(round: Int): MatchResultFragment
        {
            val fragment = MatchResultFragment()
            fragment.arguments = Bundle().apply {
                putInt(ROUND, round)
            }
            return fragment
        }
    }

    override fun getName(): String = "MatchResultFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        arguments?.getInt(ROUND)?.let { roundNumber = it }
        return inflater.inflate(R.layout.match_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        homeName = view.findViewById(R.id.match_result_home)
        awayName = view.findViewById(R.id.match_result_away)

        group = view.findViewById(R.id.match_result_group)

        sets = view.findViewById(R.id.match_result_sets)
        games = view.findViewById(R.id.match_result_gems)

        place = view.findViewById(R.id.match_result_place)
        date = view.findViewById(R.id.match_result_date)

        homeCard = view.findViewById(R.id.match_result_card_home)
        awayCard = view.findViewById(R.id.match_result_card_away)

        discussion = view.findViewById(R.id.match_result_discussion_button)

        cardHomeName = view.findViewById(R.id.match_result_card_home_name)
        cardAwayName = view.findViewById(R.id.match_result_card_away_name)

        homeResult = view.findViewById(R.id.match_result_card_home_result)
        awayResult = view.findViewById(R.id.match_result_card_away_result)

        recyclerView = view.findViewById(R.id.match_result_players_recyclerView)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        cardHomeName.setOnClickListener(null)
        cardAwayName.setOnClickListener(null)
        discussion.setOnClickListener(null)

        homeCard?.removeAllViews()
        awayCard?.removeAllViews()

        adapter?.onClick = null
        recyclerView?.adapter = null
        adapter = null
        recyclerView = null

        homeCard = null
        awayCard = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        getData()
        populateFields()
        setListeners()
    }

    //FIXME: listener sometimes not triggered
    private fun setListeners()
    {
//        cardHomeName.setOnFocusChangeListener { _, hasFocus ->
//            if(hasFocus) startTeamInfoActivity(match.homeId)
//        }
//
//        cardAwayName.setOnFocusChangeListener { _, hasFocus ->
//            if(hasFocus) startTeamInfoActivity(match.awayId)
//        }

        cardHomeName.setOnClickListener {
            startTeamInfoActivity(match.homeId)
        }

        cardAwayName.setOnClickListener {
            startTeamInfoActivity(match.awayId)
        }

        discussion.setOnClickListener {
            val intent = Intent(activity!!, MatchDiscussionActivity::class.java).apply {
                putExtra(MatchDiscussionActivity.MATCH_ID, match.id!!)
            }
            startActivity(intent)
        }
    }

    private fun startTeamInfoActivity(teamId: String)
    {
        val intent = Intent(activity!!, TeamInfoActivity::class.java).apply {
            putExtra(TeamInfoActivity.TEAM_ID, teamId)
        }
        startActivity(intent)
    }

    private fun getData()
    {
        match = matchViewModel.match.value?.let { it } ?: Match()
        round = match.rounds[roundNumber - 1]
    }

    private fun populateFields()
    {
        homeName.text = match.home
        awayName.text = match.away

        group.text = StringUtil.createLabel(match.groupName)

        sets.text = MatchRepository.getResults(round).sets
        games.text = MatchRepository.getResults(round).games

        place.text = match.place?.let { it } ?: getString(R.string.place_not_found)
        date.text = match.dateAndTime?.toMyString(getString(R.string.dateTime_format)) ?: getString(R.string.dateTime_not_found)

        cardHomeName.text = match.home
        cardAwayName.text = match.away

        getWinner()
        setupRecycler()
    }

    private fun getWinner()
    {
        when(round.homeWinner)
        {
            null -> {
                setResultButton(homeResult, "-", R.color.lightGrey)
                setResultButton(awayResult, "-", R.color.lightGrey)
            }
            true -> {
                setResultButton(homeResult, getString(R.string.winner_acronym), R.color.blue)
                setResultButton(awayResult, getString(R.string.loser_acronym), R.color.red)
            }
            else -> {
                setResultButton(homeResult, getString(R.string.loser_acronym), R.color.red)
                setResultButton(awayResult, getString(R.string.winner_acronym), R.color.blue)
            }
        }
    }

    private fun setResultButton(button: Button, text: String, @ColorRes colorRes: Int)
    {
        button.text = text
        button.backgroundTintList = ColorStateList.valueOf(App.getColor(colorRes))
    }

    private fun setupRecycler()
    {
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = ShowPlayersAdapter(round.homePlayers + round.awayPlayers, winners = getWinners())

        adapter?.onClick = { player ->
            val intent = Intent(activity, PlayerInfoActivity::class.java).apply {
                putExtra(PlayerInfoActivity.PLAYER_ID, player.playerId)
            }
            startActivity(intent)
        }
        recyclerView?.adapter = adapter
    }

    private fun getWinners(): List<Boolean?>
    {
        val homeList = List(round.homePlayers.size) { round.homeWinner }
        val awayList = List(round.awayPlayers.size) { round.homeWinner?.let { !it } }
        return homeList + awayList
    }
}