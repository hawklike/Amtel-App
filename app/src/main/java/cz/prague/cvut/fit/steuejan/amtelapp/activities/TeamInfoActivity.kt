package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.github.mikephil.charting.charts.PieChart
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal.ShowPlayersAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.paging.ShowTeamMatchesPagingAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.toPlayer
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.RankingOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.miscellaneous.MapFragment
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities.TeamInfoActivityVM
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments.RankingFragmentVM

class TeamInfoActivity : AbstractProfileActivity()
{
    private val viewModel by viewModels<TeamInfoActivityVM>()

    private lateinit var chartMatchesThisYear: PieChart
    private lateinit var chartMatchesTotal: PieChart
    private lateinit var chartSets: PieChart

    private var teamId: String = ""

    private var matchesRecyclerView: RecyclerView? = null
    private var matchesAdapter: ShowTeamMatchesPagingAdapter? = null

    private var playersRecyclerView: RecyclerView? = null
    private var playersAdapter: ShowPlayersAdapter? = null

    companion object
    {
        const val TEAM = "team"
        const val RANKING = "ranking"
        const val TEAM_ID = "teamId"
    }

    override fun onDestroy()
    {
        super.onDestroy()
        if(::chartMatchesThisYear.isInitialized) chartMatchesThisYear.setOnChartValueSelectedListener(null)
        if(::chartMatchesTotal.isInitialized) chartMatchesTotal.setOnChartValueSelectedListener(null)
        if(::chartSets.isInitialized) chartSets.setOnChartValueSelectedListener(null)

        matchesAdapter?.onClick = null
        matchesRecyclerView?.adapter = null
        matchesAdapter = null
        matchesRecyclerView = null

        playersAdapter?.onClick = null
        playersRecyclerView?.adapter = null
        playersAdapter = null
        playersRecyclerView = null
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.team_info)
        super.onCreate(savedInstanceState)
        getData()
        setToolbarTitle("Načítám tým...")
        setArrowBack()
        initAll()
    }

    private fun getData()
    {
        intent.extras?.let { bundle ->
            viewModel.mTeam = bundle.getParcelable(TEAM)
            teamId = bundle.getString(TEAM_ID) ?: teamId
            bundle.getInt(RANKING).let { rank ->
                if(rank != 0) viewModel.setTeamRank(rank)
            }
        }
    }

    private fun initAll()
    {
       if(viewModel.mTeam != null)
       {
           setToolbarTitle(viewModel.mTeam!!.name)
           setCharts()
           setTotalMatches()
           setSuccessRate()
           setActualGroup()
           setCurrentRank()
           setAverageRank()
           setTitles()
           setMatches()
           setPlayers()
           setMap()
       }
       else viewModel.getTeam(teamId)

       viewModel.team.observe(this) {
           viewModel.mTeam = it
           setToolbarTitle(it.name)
           initAll()
       }
    }

    private fun setPlayers()
    {
        playersRecyclerView = findViewById(R.id.team_info_players_recyclerView)
        playersRecyclerView?.setHasFixedSize(true)
        playersRecyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val team = viewModel.mTeam ?: Team()
        playersAdapter = ShowPlayersAdapter(
            team.users.map { it.toPlayer() },
            true,
            team = team
        )

        playersAdapter?.onClick = { player ->
            val intent = Intent(this, PlayerInfoActivity::class.java).apply {
                putExtra(PlayerInfoActivity.PLAYER_ID, player.playerId)
            }
            startActivity(intent)
        }

        playersRecyclerView?.adapter = playersAdapter
    }

    private fun setMatches()
    {
        matchesRecyclerView = findViewById(R.id.team_info_matches_recyclerView)
        matchesRecyclerView?.setHasFixedSize(true)
        matchesRecyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val query = TeamRepository.retrieveMatches(viewModel.mTeam ?: Team(id = "hlen"))
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(6)
            .setPageSize(4)
            .build()

        val options = FirestorePagingOptions.Builder<Match>()
            .setLifecycleOwner(this)
            .setQuery(query, config, Match::class.java)
            .build()

        matchesAdapter = ShowTeamMatchesPagingAdapter(viewModel.mTeam ?: Team(id = "mucus"), options)
        matchesAdapter?.onClick = { match ->
            val intent = Intent(this, MatchViewPagerActivity::class.java).apply {
                putExtra(MatchViewPagerActivity.MATCH, match)
                putExtra(MatchViewPagerActivity.TITLE, getString(R.string.match_result))
            }
            startActivity(intent)
        }

        matchesRecyclerView?.adapter = matchesAdapter
    }

    private fun setTotalMatches()
    {
        val matches = findViewById<TextView>(R.id.team_info_matches)
        viewModel.matches.observe(this) { matches.text = it.toString() }
    }

    private fun setTitles()
    {
        val titles = findViewById<TextView>(R.id.team_info_titles)
        viewModel.titles.observe(this) { titles.text = it.toString() }
    }

    private fun setAverageRank()
    {
        val averageRank = findViewById<TextView>(R.id.team_info_avgRank)
        viewModel.getAverageRank()
        viewModel.avgRank.observe(this) { averageRank.text = it.toString() }
    }

    private fun setCurrentRank()
    {
        val teamRank = findViewById<TextView>(R.id.team_info_rank)
        if(viewModel.teamRank.value == null)
        {
            val rankingViewModel by viewModels<RankingFragmentVM>()
            viewModel.mTeam?.groupId?.let { rankingViewModel.loadTeams(it, DateUtil.actualSeason.toInt(), RankingOrderBy.POINTS) }
            rankingViewModel.teams.observe(this) {
                viewModel.calculateTeamRank(it)
            }
        }

        viewModel.teamRank.observe(this) { teamRank.text = it.toString() }
    }

    private fun setActualGroup()
    {
        val group = findViewById<TextView>(R.id.team_info_group)
        group.text = viewModel.mTeam?.groupName ?: ""
    }

    @SuppressLint("SetTextI18n")
    private fun setSuccessRate()
    {
        val successRate = findViewById<TextView>(R.id.team_info_successRate)
        viewModel.successRate.observe(this) { successRate.text = "$it %" }
    }

    private fun setCharts()
    {
        chartMatchesThisYear = findViewById(R.id.team_info_chart_matches_thisYear)
        chartMatchesTotal = findViewById(R.id.team_info_chart_games_total)
        chartSets = findViewById(R.id.team_info_chart_sets)

        viewModel.getChartsData()
        viewModel.charts.observe(this) { entries ->
            chartMatchesThisYear.visibility = VISIBLE
            chartMatchesTotal.visibility = VISIBLE
            chartSets.visibility = VISIBLE

            if(entries.first.size == 2) initChart(chartMatchesThisYear, entries.first, getString(R.string.play))
            else initChart(chartMatchesThisYear, entries.first, getString(R.string.play), R.color.lightGrey)

            if(entries.second.size == 2) initChart(chartMatchesTotal,entries.second, getString(R.string.Total))
            else initChart(chartMatchesTotal,entries.second, getString(R.string.Total), R.color.lightGrey)

            if(entries.third.size == 2) initChart(chartSets, entries.third, getString(R.string.sets))
            else initChart(chartSets, entries.third, getString(R.string.sets), R.color.lightGrey)
        }
    }

    private fun setMap()
    {
        supportFragmentManager.commit {
            replace(R.id.team_info_map_container, MapFragment.newInstance(viewModel.mTeam?.place))
        }
    }
}