package cz.prague.cvut.fit.steuejan.amtelapp.fragments.miscellaneous

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.InputReportActivity
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.paging.ShowReportsAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Report
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.LeagueRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.ReportRepository
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.FragmentReportsBinding
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment

class ReportsFragment : AbstractMainActivityFragment()
{
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private var adapter: ShowReportsAdapter? = null

    companion object
    {
        fun newInstance(): ReportsFragment = ReportsFragment()
    }

    override fun getName(): String = "ReportsFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle(getString(R.string.reports))
        setLogoutIconVisibility(false)
        createReport()
        showReports()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        binding.addReport.setOnClickListener(null)
        adapter?.onClick = null
        adapter?.onEdit = null
        binding.reports.adapter = null
        adapter = null
        _binding = null
    }

    private fun createReport()
    {
        if(AuthManager.currentUser?.uid != null && AuthManager.currentUser?.uid == LeagueRepository.headOfLeague?.id)
        {
            with(binding) {
                addReport.visibility = VISIBLE
                addReport.setOnClickListener {
                    val intent = Intent(activity, InputReportActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun showReports()
    {
        with(binding) {
            reports.setHasFixedSize(true)
            reports.layoutManager = LinearLayoutManager(activity)
        }

        val query = ReportRepository.retrieveAllReports()
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(6)
            .setPageSize(3)
            .build()

        val options = FirestorePagingOptions.Builder<Report>()
            .setQuery(query, config, Report::class.java)
            .setLifecycleOwner(this)
            .build()

        adapter = ShowReportsAdapter(options)

        binding.reports.adapter = adapter
    }
}