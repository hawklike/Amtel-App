package cz.prague.cvut.fit.steuejan.amtelapp.activities

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

abstract class AbstractProfileActivity : AbstractBaseActivity(), OnChartValueSelectedListener
{
    protected fun initChart(
        chart: PieChart,
        entries: List<PieEntry>,
        title: String,
        @ColorRes vararg colors: Int = intArrayOf(R.color.blue, R.color.red))
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