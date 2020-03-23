package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.activity.viewModels
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.TeamInfoActivityVM

class TeamInfoActivity : AbstractBaseActivity()
{
    private val viewModel by viewModels<TeamInfoActivityVM>()

    private lateinit var chartMatches: PieChart

    private lateinit var team: Team

    companion object
    {
        const val TEAM = "team"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.team_info)
        super.onCreate(savedInstanceState)
        setToolbarTitle("Profil týmu")
        intent.extras?.let { team = it.getParcelable(TEAM) ?: Team() }
        setArrowBack()
        setChart()
    }

    private fun setChart()
    {
        chartMatches = findViewById(R.id.team_info_chart_matches)
        val matchesEntries = listOf(PieEntry(144f, "Výhry"), PieEntry(18f, "Prohry"))


        val dataSet = PieDataSet(matchesEntries, "Zápasy")
        dataSet.setColors(App.getColor(R.color.blue), App.getColor(R.color.red))
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = App.getColor(R.color.white)

        val pieData = PieData(dataSet)

        pieData.setValueFormatter(CustomFormatter())
        chartMatches.setHoleColor(App.getColor(R.color.veryVeryLightGrey))
        chartMatches.data = pieData
        chartMatches.legend.isEnabled = false
        chartMatches.description.isEnabled = false
        chartMatches.isHighlightPerTapEnabled = false
        chartMatches.holeRadius = 48f
        chartMatches.transparentCircleRadius = 48f
        chartMatches.animateY(1400, Easing.EaseInOutQuad)
        chartMatches.centerText = "Zápasy"

        chartMatches.setEntryLabelColor(App.getColor(R.color.white))
        chartMatches.setEntryLabelTextSize(13f)
        chartMatches.setCenterTextSize(16f)
        chartMatches.setCenterTextColor(App.getColor(R.color.darkGrey))
        chartMatches.setDrawCenterText(true)
        chartMatches.invalidate()
    }

    class CustomFormatter : ValueFormatter()
    {
        override fun getFormattedValue(value: Float): String = "${value.toInt()}"
    }
}