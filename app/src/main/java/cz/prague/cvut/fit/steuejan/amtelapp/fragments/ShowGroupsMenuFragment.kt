package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.RankingActivity
import cz.prague.cvut.fit.steuejan.amtelapp.activities.ScheduleRoundsActivity
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowGroupsMenuAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ShowGroupsMenuFragmentVM

class ShowGroupsMenuFragment : AbstractMainActivityFragment()
{
    private val viewModel by viewModels<ShowGroupsMenuFragmentVM>()

    private var isRanking = true

    private lateinit var progressBar: FrameLayout

    private var recyclerView: RecyclerView? = null
    private var adapter: ShowGroupsMenuAdapter? = null

    companion object
    {
        private const val IS_RANKING = "ranking"

        fun newInstance(isRanking: Boolean): ShowGroupsMenuFragment
        {
            val fragment = ShowGroupsMenuFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(IS_RANKING, isRanking)
            }
            return fragment
        }
    }

    override fun getName(): String = "ScheduleFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        arguments?.getBoolean(IS_RANKING)?.let { isRanking = it }
        return inflater.inflate(R.layout.show_groups_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.show_groups_recyclerView)
        progressBar = view.findViewById(R.id.show_groups_progressBar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        if(isRanking) setToolbarTitle(getString(R.string.results))
        else setToolbarTitle(getString(R.string.schedule))
        viewModel.loadGroups()
        setupRecycler()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        recyclerView = null
        adapter = null
    }

    private fun setupRecycler()
    {
        viewModel.groups.observe(viewLifecycleOwner) {
            progressBar.visibility = View.GONE
            recyclerView?.setHasFixedSize(true)
            recyclerView?.layoutManager = LinearLayoutManager(activity!!)
            adapter = ShowGroupsMenuAdapter(activity!!, it.self, isRanking)
            onNextClick(adapter)
            recyclerView?.adapter = adapter
        }
    }

    private fun onNextClick(adapter: ShowGroupsMenuAdapter?)
    {
        if(isRanking)
        {
            adapter?.onNextClick = { group, _ ->
                val intent = Intent(activity!!, RankingActivity::class.java).apply {
                    putExtra(RankingActivity.GROUP, group)
                }
                if(group.teamIds.isNotEmpty()) startActivity(intent)
            }
        }
        else
        {
            adapter?.onNextClick = { group, actualRound ->
                val intent = Intent(activity!!, ScheduleRoundsActivity::class.java).apply {
                    putExtra(ScheduleRoundsActivity.GROUP, group)
                    putExtra(ScheduleRoundsActivity.USER, mainActivityModel.getUser().value)
                    putExtra(ScheduleRoundsActivity.ACTUAL_ROUND, actualRound)
                }
                if(group.rounds[DateUtil.actualYear] != 0) startActivity(intent)
            }
        }
    }
}