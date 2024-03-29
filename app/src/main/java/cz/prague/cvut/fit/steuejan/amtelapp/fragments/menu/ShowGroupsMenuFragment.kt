package cz.prague.cvut.fit.steuejan.amtelapp.fragments.menu

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
import cz.prague.cvut.fit.steuejan.amtelapp.activities.PlayoffActivity
import cz.prague.cvut.fit.steuejan.amtelapp.activities.RankingViewPagerActivity
import cz.prague.cvut.fit.steuejan.amtelapp.activities.RoundsViewPagerActivity
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal.ShowGroupsMenuAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments.ShowGroupsMenuFragmentVM

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
        setLogoutIconVisibility(false)
        if(isRanking) setToolbarTitle(getString(R.string.results))
        else setToolbarTitle(getString(R.string.schedule))
        viewModel.loadGroups()
        setupRecycler()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        adapter?.onNextClick = null
        recyclerView?.adapter = null
        adapter = null
        recyclerView = null
    }

    private fun setupRecycler()
    {
        viewModel.groups.observe(viewLifecycleOwner) {
            progressBar.visibility = View.GONE
            recyclerView?.setHasFixedSize(true)
            recyclerView?.layoutManager = LinearLayoutManager(activity!!)
            adapter = ShowGroupsMenuAdapter(
                activity!!,
                it,
                isRanking,
                mainActivityModel.getUser().value
            )
            onNextClick(adapter)
            recyclerView?.adapter = adapter
        }
    }

    private fun onNextClick(adapter: ShowGroupsMenuAdapter?)
    {
        if(isRanking)
        {
            adapter?.onNextClick = { group, _ ->
                val intent = Intent(activity!!, RankingViewPagerActivity::class.java).apply {
                    putExtra(RankingViewPagerActivity.GROUP, group)
                }
                if(group.teamIds.isNotEmpty()) startActivity(intent)
            }
        }
        else
        {
            adapter?.onNextClick = { group, actualRound ->
                if(group.playOff)
                {
                    val intent = Intent(activity!!, PlayoffActivity::class.java).apply {
                        putExtra(PlayoffActivity.GROUP, group)
                        putExtra(PlayoffActivity.USER, mainActivityModel.getUser().value)
                    }
                    startActivity(intent)
                }
                else
                {
                    val intent = Intent(activity!!, RoundsViewPagerActivity::class.java).apply {
                        putExtra(RoundsViewPagerActivity.GROUP, group)
                        putExtra(RoundsViewPagerActivity.USER, mainActivityModel.getUser().value)
                        putExtra(RoundsViewPagerActivity.ACTUAL_ROUND, actualRound)
                    }
                    if(group.rounds[DateUtil.actualSeason] != 0) startActivity(intent)
                }
            }
        }
    }
}