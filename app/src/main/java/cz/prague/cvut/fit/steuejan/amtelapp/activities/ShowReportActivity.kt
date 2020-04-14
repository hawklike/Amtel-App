package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Report
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.ShowReportBinding

class ShowReportActivity : AbstractBaseActivity()
{
    private lateinit var binding: ShowReportBinding

    private lateinit var report: Report

    companion object
    {
        const val REPORT = "report"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        binding = ShowReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        getReport()
        setArrowBack()
    }

    private fun getReport()
    {
        intent.extras?.let { bundle ->
            report = bundle.getParcelable(REPORT) ?: Report()
        }

        setToolbarTitle(report.title)

        with(binding) {
            lead.text = report.lead
            text.text = report.text
        }
    }

}