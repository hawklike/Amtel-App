package cz.prague.cvut.fit.steuejan.amtelapp.fragments.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowPlayersAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
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

    private lateinit var sets: TextView
    private lateinit var games: TextView

    private lateinit var place: TextView
    private lateinit var date: TextView

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

        sets = view.findViewById(R.id.match_result_sets)
        games = view.findViewById(R.id.match_result_gems)

        place = view.findViewById(R.id.match_result_place)
        date = view.findViewById(R.id.match_result_date)

        homeCard = view.findViewById(R.id.match_result_card_home)
        awayCard = view.findViewById(R.id.match_result_card_away)

        cardHomeName = view.findViewById(R.id.match_result_card_home_name)
        cardAwayName = view.findViewById(R.id.match_result_card_away_name)

        recyclerView = view.findViewById(R.id.match_result_players_recyclerView)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        cardHomeName.setOnClickListener(null)
        cardAwayName.setOnClickListener(null)

        homeCard?.removeAllViews()
        awayCard?.removeAllViews()

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

    //TODO
    private fun setListeners()
    {
        cardHomeName.setOnClickListener {
            toast(match.homeId)
        }

        cardAwayName.setOnClickListener {
            toast(match.awayId)
        }

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

        sets.text = MatchManager.getResults(round).sets
        games.text = MatchManager.getResults(round).games

        place.text = match.place?.let { it } ?: getString(R.string.place_not_found)
        date.text = match.dateAndTime?.toMyString(getString(R.string.dateTime_format)) ?: getString(R.string.dateTime_not_found)

        cardHomeName.text = match.home
        cardAwayName.text = match.away

        setupRecycler()
    }

    private fun setupRecycler()
    {
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = ShowPlayersAdapter(round.homePlayers + round.awayPlayers)
        adapter?.onClick = {
            //TODO
        }
        recyclerView?.adapter = adapter
    }
}