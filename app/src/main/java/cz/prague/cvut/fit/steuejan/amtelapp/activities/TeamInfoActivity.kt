package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.lifecycle.observe
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
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
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

    companion object
    {
        const val TEAM = "team"
        const val SEASON_TABLE = "ranking"
        const val TEAM_ID = "teamId"
    }

    override fun onDestroy()
    {
        super.onDestroy()
        chartMatchesThisYear.setOnChartValueSelectedListener(null)
        chartMatchesTotal.setOnChartValueSelectedListener(null)
        chartSets.setOnChartValueSelectedListener(null)
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
        setToolbarTitle("Profil týmu")
        setArrowBack()
        initAll()
    }

    private fun initAll()
    {
       if(viewModel.mTeam != null)
       {
           setTeamName()
           setCharts()
           setSuccessRate()
           setActualGroup()
           setCurrentRank()
           setAverageRank()
           setTitles()
       }
       else viewModel.getTeam(teamId)

       viewModel.team.observe(this) {
           viewModel.mTeam = it
           initAll()
       }
    }

    private fun setTitles()
    {
        val titles = findViewById<TextView>(R.id.team_info_titles)
        viewModel.titles.observe(this) {
            titles.text = "Tituly: $it"
        }
    }

    private fun setAverageRank()
    {
        val averageRank = findViewById<TextView>(R.id.team_info_avgRank)
        viewModel.getAverageRank()
        viewModel.avgRank.observe(this) {
            averageRank.text = "Průměrné umístění: $it."
        }
    }

    private fun setTeamName()
    {
        val teamName = findViewById<TextView>(R.id.team_info_overview_title)
        teamName.text = viewModel.mTeam?.name
    }

    private fun setCurrentRank()
    {
        val teamRank = findViewById<TextView>(R.id.team_info_rank)
        if(viewModel.seasonRanking.isNotEmpty()) viewModel.calculateTeamRank()
        else
        {
            val rankingViewModel by viewModels<RankingFragmentVM>()
            viewModel.mTeam?.group?.let { rankingViewModel.loadTeams(it, DateUtil.actualYear.toInt(), RankingOrderBy.POINTS) }
            rankingViewModel.teams.observe(this) {
                viewModel.seasonRanking = it
                viewModel.calculateTeamRank()
            }
        }

        viewModel.teamRank.observe(this) { teamRank.text = "Pozice v tabulce: $it." }
    }

    private fun setActualGroup()
    {
        val group = findViewById<TextView>(R.id.team_info_group)
        group.text = "Aktuální skupina: ${viewModel.mTeam?.group ?: "bez skupiny"}"
    }

    private fun setSuccessRate()
    {
        val successRate = findViewById<TextView>(R.id.team_info_successRate)
        viewModel.successRate.observe(this) { successRate.text = "Úspěšnost: $it %" }
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

            initChart(chartMatchesThisYear, entries.first, getString(R.string.play))
            initChart(chartMatchesTotal,entries.second, getString(R.string.Total))
            initChart(chartSets, entries.third, getString(R.string.sets))
        }
    }

    private fun initChart(chart: PieChart, entries: List<PieEntry>, title: String, @ColorRes vararg colors: Int = intArrayOf(R.color.blue, R.color.red))
    {
        val dataSet = PieDataSet(entries, title)
        dataSet.colors = colors.map { App.getColor(it) }
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = App.getColor(R.color.white)

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
        toast(pie?.label ?: "Můj vedoucí bakalářky Tomáš Nováček je borec, má však málo hlenu.")
    }
}