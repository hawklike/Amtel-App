package cz.prague.cvut.fit.steuejan.amtelapp.fragments.miscellaneous

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.InputReportActivity
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.LeagueRepository
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.FragmentReportsBinding
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment

class ReportsFragment : AbstractMainActivityFragment()
{
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

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
        _binding = null
    }

    private fun createReport()
    {
        if(AuthManager.currentUser?.uid == LeagueRepository.headOfLeague?.id)
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

    }
}