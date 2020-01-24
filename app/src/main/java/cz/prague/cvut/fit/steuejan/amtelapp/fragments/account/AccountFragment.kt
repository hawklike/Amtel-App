package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ViewPagerAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole.HEAD_OF_LEAGUE
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole.TEAM_MANAGER
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.AbstractBaseFragment


class AccountFragment : AbstractBaseFragment()
{
    companion object
    {
        const val DATA = "user"

        fun newInstance(user: User): AccountFragment
        {
            val fragment = AccountFragment()
            val bundle = Bundle()
            bundle.putParcelable(DATA, user)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var viewPager: ViewPager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle(getString(R.string.account))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val user = arguments?.get(DATA) as User

        viewPager = view.findViewById(R.id.account_viewPager)
        setupViewPager(viewPager, user)

        val tabs: TabLayout = view.findViewById(R.id.account_tabs)
        tabs.setupWithViewPager(viewPager)
    }

    private fun setupViewPager(viewPager: ViewPager, user: User)
    {
        val adapter = ViewPagerAdapter(childFragmentManager)

        val role = UserRole.toRole(user.role)
        if(role == HEAD_OF_LEAGUE)
        {
            adapter.addFragment(AccountBossAddTMFragment.newInstance(), getString(R.string.account_boss_adapter_add_TM))
            adapter.addFragment(AccountBossMakeGroupsFragment.newInstance(), getString(R.string.account_boss_adapter_make_groups))
            adapter.addFragment(AccountPersonalFragment.newInstance(user), getString(R.string.account_adapter_personal))
        }
        else if(role == TEAM_MANAGER)
        {
            adapter.addFragment(AccountPersonalFragment.newInstance(user), getString(R.string.account_adapter_personal))
            adapter.addFragment(AccountTMMakeTeamFragment.newInstance(), getString(R.string.account_tm_adapter_make_team))
        }

        viewPager.adapter = adapter
    }
}