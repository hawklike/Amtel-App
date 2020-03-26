package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.lifecycle.observe
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowPlayersAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowTeamMatchesPagingAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.toPlayer
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.RankingOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.RankingFragmentVM
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.TeamInfoActivityVM

class TeamInfoActivity : AbstractBaseActivity(), OnChartValueSelectedListener
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
        const val SEASON_TABLE = "ranking"
        const val TEAM_ID = "teamId"
    }

    override fun onDestroy()
    {
        super.onDestroy()
        if(::chartMatchesThisYear.isInitialized)
            chartMatchesThisYear.setOnChartValueSelectedListener(null)
        if(::chartMatchesTotal.isInitialized)
            chartMatchesTotal.setOnChartValueSelectedListener(null)
        if(::chartSets.isInitialized)
            chartSets.setOnChartValueSelectedListener(null)

        matchesRecyclerView?.adapter = null
        matchesAdapter = null
        matchesRecyclerView = null

        playersRecyclerView?.adapter = null
        playersAdapter = null
        playersRecyclerView = null
    }

    override fun onStop()
    {
        super.onStop()
        matchesAdapter?.stopListening()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.team_info)
        super.onCreate(savedInstanceState)
        intent.extras?.let { bundle ->
            viewModel.mTeam = bundle.getParcelable(TEAM)
            teamId = bundle.getString(TEAM_ID) ?: teamId
            bundle.getParcelableArrayList<Team>(SEASON_TABLE)?.toList()?.let { viewModel.seasonRanking = it }
        }
        setToolbarTitle("Název týmu se načítá...")
        setArrowBack()
        initAll()
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
        playersAdapter = ShowPlayersAdapter(team.users.map { it.toPlayer() }, true)

        playersRecyclerView?.adapter = playersAdapter
    }

    private fun setMatches()
    {
        matchesRecyclerView = findViewById(R.id.team_info_matches_recyclerView)
        matchesRecyclerView?.setHasFixedSize(true)
        matchesRecyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val query = TeamManager.retrieveMatches(viewModel.mTeam ?: Team(id = "hlen"))
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(4)
            .setPageSize(10)
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

        matchesAdapter?.startListening()
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
        if(viewModel.seasonRanking.isNotEmpty()) viewModel.calculateTeamRank()
        else
        {
            val rankingViewModel by viewModels<RankingFragmentVM>()
            viewModel.mTeam?.groupId?.let { rankingViewModel.loadTeams(it, DateUtil.actualYear.toInt(), RankingOrderBy.POINTS) }
            rankingViewModel.teams.observe(this) {
                viewModel.seasonRanking = it
                viewModel.calculateTeamRank()
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

    private fun initChart(chart: PieChart, entries: List<PieEntry>, title: String, @ColorRes vararg colors: Int = intArrayOf(R.color.blue, R.color.red))
    {
        val dataSet = PieDataSet(entries, title)
        dataSet.colors = colors.map { App.getColor(it) }
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor =
            if(colors.size == 1) App.getColor(colors.first())
            else App.getColor(R.color.white)

        val data = PieData(dataSet)

        data.setValueFormatter(CustomFormatter())
        chart.setOnChartValueSelectedListener(this)

        chart.legend.isEnabled = false
        chart.description.isEnabled = false

        chart.holeRadius = 48f
        chart.transparentCircleRadius = 48f
        chart.setHoleColor(App.getColor(R.color.veryVeryLightGrey))

        chart.setDrawEntryLabels(false)

        chart.centerText = title
        chart.setCenterTextSize(16f)
        chart.setCenterTextColor(App.getColor(R.color.darkGrey))

        chart.data = data
        chart.animateY(1400, Easing.EaseInOutQuad)
        chart.invalidate()
    }

    class CustomFormatter : ValueFormatter()
    {
        override fun getFormattedValue(value: Float): String
                = if(value.toInt() == 0) "" else "${value.toInt()}"
    }

    override fun onNothingSelected() {}

    override fun onValueSelected(e: Entry, h: Highlight)
    {
        val pie = e as? PieEntry
        toast(pie?.label ?: "Můj vedoucí bakalářky Tomáš Nováček je borec, dokonce i abstinent.")
    }
}