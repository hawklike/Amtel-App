package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.ColorRes
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
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.TeamInfoActivityVM

class TeamInfoActivity : AbstractBaseActivity(), OnChartValueSelectedListener
{
    private val viewModel by viewModels<TeamInfoActivityVM>()

    private lateinit var chartMatches: PieChart
    private lateinit var chartSets: PieChart
    private lateinit var chartGames: PieChart

    private var team = Team()
    private var seasonRanking = listOf<Team>()

    companion object
    {
        const val TEAM = "team"
        const val SEASON_TABLE = "ranking"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.team_info)
        super.onCreate(savedInstanceState)
        intent.extras?.let { bundle ->
            team = bundle.getParcelable(TEAM) ?: Team()
            bundle.getParcelableArrayList<Team>(SEASON_TABLE)?.toList()?.let { seasonRanking = it }
        }
        setToolbarTitle(team.name)
        setArrowBack()
        setChart()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        chartMatches.setOnChartValueSelectedListener(null)
        chartSets.setOnChartValueSelectedListener(null)
        chartGames.setOnChartValueSelectedListener(null)
    }

    private fun setChart()
    {
        chartMatches = findViewById(R.id.team_info_chart_matches)
        chartSets = findViewById(R.id.team_info_chart_sets)
        chartGames = findViewById(R.id.team_info_chart_games)

        val matchesEntries = listOf(PieEntry(144f, "Výhry"), PieEntry(18f, "Prohry"))
        initChart(chartMatches, matchesEntries, getString(R.string.matches))

        val setsEntries = listOf(PieEntry(18f, "Získané sety"), PieEntry(6f, "Ztracené sety"))
        initChart(chartSets, setsEntries, getString(R.string.sets))

        val gamesEntries = listOf(PieEntry(64f, "Získané gemy"), PieEntry(43f, "Ztracené gemy"))
        initChart(chartGames, gamesEntries, getString(R.string.games))
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
        override fun getFormattedValue(value: Float): String = "${value.toInt()}"
    }

    override fun onNothingSelected() {}

    override fun onValueSelected(e: Entry, h: Highlight)
    {
        val pie = e as? PieEntry
        toast(pie?.label ?: "Můj vedoucí bakalářky Tomáš Nováček je borec, má však málo hlenu.")
    }
}