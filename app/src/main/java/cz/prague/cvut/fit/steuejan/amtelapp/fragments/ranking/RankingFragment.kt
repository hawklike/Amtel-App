package cz.prague.cvut.fit.steuejan.amtelapp.fragments.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowTeamsRankingAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.RankingOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractBaseFragment
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.RankingFragmentVM

class RankingFragment : AbstractBaseFragment()
{
    private val viewModel by viewModels<RankingFragmentVM>()

    private var year = 0
    private var group = Group()

    private val teams: MutableList<Team> = mutableListOf()
    private var orderBy = RankingOrderBy.POINTS

    private lateinit var actualSortOption: TextView

    private lateinit var sortByMatches: TextView
    private lateinit var sortByWins: TextView
    private lateinit var sortByLosses: TextView
    private lateinit var sortByPositiveSets: TextView
    private lateinit var sortByNegativeSets: TextView
    private lateinit var sortByPoints: TextView

    private var recyclerView: RecyclerView? = null
    private var adapter: ShowTeamsRankingAdapter? = null

    companion object
    {
        private const val GROUP = "group"
        private const val YEAR = "year"

        fun newInstance(group: Group, year: Int): RankingFragment
        {
            val fragment = RankingFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(GROUP, group)
                putInt(YEAR, year)
            }
            return fragment
        }
    }

    override fun getName(): String = "RankingFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        arguments?.getParcelable<Group>(GROUP)?.let { group = it }
        arguments?.getInt(YEAR)?.let { year = it }
        return inflater.inflate(R.layout.ranking_fragment, container, false)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        recyclerView?.adapter = null
        recyclerView = null
        adapter = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        sortByMatches = view.findViewById(R.id.ranking_matches)
        sortByWins = view.findViewById(R.id.ranking_wins)
        sortByLosses = view.findViewById(R.id.ranking_losses)
        sortByPositiveSets = view.findViewById(R.id.ranking_sets_positive)
        sortByNegativeSets = view.findViewById(R.id.ranking_sets_negative)
        sortByPoints = view.findViewById(R.id.ranking_points)
        recyclerView = view.findViewById(R.id.ranking_recyclerView)
        actualSortOption = sortByPoints
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        loadData()
        setupRecycler()
        setBoldStyle(sortByPoints)
        setListeners()
    }

    private fun loadData()
    {
        viewModel.loadTeams(group.name, year)
    }

    //TODO: implement sorting teams
    private fun setupRecycler()
    {
        viewModel.teams.observe(viewLifecycleOwner) {
            teams.addAll(it)
            recyclerView?.setHasFixedSize(true)
            recyclerView?.layoutManager = LinearLayoutManager(activity!!)
            adapter = ShowTeamsRankingAdapter(teams, year.toString(), orderBy)
            recyclerView?.adapter = adapter
        }
    }

    private fun setListeners()
    {
        sortByMatches.setOnClickListener {
            highlightOption(it as TextView)
            //TODO
        }

        sortByWins.setOnClickListener {
            highlightOption(it as TextView)
            //TODO
        }

        sortByLosses.setOnClickListener {
            highlightOption(it as TextView)
            //TODO
        }

        sortByPositiveSets.setOnClickListener {
            highlightOption(it as TextView)
            //TODO
        }

        sortByNegativeSets.setOnClickListener {
            highlightOption(it as TextView)
            //TODO
        }

        sortByPoints.setOnClickListener {
            highlightOption(it as TextView)
            //TODO
        }
    }

    private fun highlightOption(view: TextView)
    {
        setNormalStyle(actualSortOption)
        setBoldStyle(view)
        actualSortOption = view
    }

    private fun setNormalStyle(view: TextView)
    {
        view.setTextColor(App.getColor(R.color.darkGrey))
    }

    private fun setBoldStyle(view: TextView)
    {
        view.setTextColor(App.getColor(R.color.blue))
    }

}