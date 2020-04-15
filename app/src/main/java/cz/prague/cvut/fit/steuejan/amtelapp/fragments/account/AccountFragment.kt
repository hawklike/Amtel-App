package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ViewPagerAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole.HEAD_OF_LEAGUE
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole.TEAM_MANAGER
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.NoTeam
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class AccountFragment : AbstractMainActivityFragment()
{
    companion object
    {
        fun newInstance(): AccountFragment = AccountFragment()
    }

    override val job: Job = Job()

    private lateinit var viewPager: ViewPager
    private lateinit var tabs: TabLayout
    private lateinit var user: User

    override fun getName(): String = "AccountFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onDestroy()
    {
        job.cancel()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        viewPager = view.findViewById(R.id.account_viewPager)
        tabs = view.findViewById(R.id.account_tabs)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setLogoutIconVisibility(true)
        setToolbarTitle(getString(R.string.account))
        getUser()
        setupViewPager(viewPager, user)
        tabs.setupWithViewPager(viewPager)
    }

    private fun getUser()
    {
        user = mainActivityModel.getUser().value ?: User()
        mainActivityModel.getUser().observe(viewLifecycleOwner) { observedUser ->
            user = observedUser?.copy() ?: user
            Log.i("AccountFragment", "getUser(): user $user observed")
        }
    }

    private fun setupViewPager(viewPager: ViewPager, user: User)
    {
        val adapter = ViewPagerAdapter(childFragmentManager)

        tabs.tabMode = TabLayout.MODE_FIXED
        tabs.tabGravity = TabLayout.GRAVITY_FILL
        tabs.layoutParams.width= ViewGroup.LayoutParams.MATCH_PARENT

        val role = user.role.toRole()
        if(role == HEAD_OF_LEAGUE)
        {
            adapter.addFragment(AccountBossMakeGroupsFragment.newInstance(), getString(R.string.account_boss_adapter_make_groups))
            adapter.addFragment(AccountBossAddTMFragment.newInstance(), getString(R.string.account_boss_adapter_add_TM))
            adapter.addFragment(AccountPersonalFragment.newInstance(), getString(R.string.account_adapter_personal))
        }
        else if(role == TEAM_MANAGER)
        {
            launch {
                setProgressBar(true)
                user.teamId?.let {
                    val team = TeamRepository.findTeam(it)
                    if(team is ValidTeam) mainActivityModel.setTeam(team)
                } ?: mainActivityModel.setTeam(NoTeam)

                adapter.addFragment(AccountTMMakeTeamFragment.newInstance(), getString(R.string.account_tm_adapter_make_team))
                adapter.addFragment(AccountPersonalFragment.newInstance(), getString(R.string.account_adapter_personal))
                adapter.notifyDataSetChanged()
                setProgressBar(false)
            }
        }
        viewPager.adapter = adapter
    }
}