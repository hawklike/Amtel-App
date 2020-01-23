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

    //TODO: add 'Ostatní', where a user may change his password
    private fun setupViewPager(viewPager: ViewPager, user: User)
    {
        //TODO: create adapter based on user status
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(AccountBossAddTMFragment.newInstance(), "Nový vedoucí")
        adapter.addFragment(AccountBossMakeGroupsFragment.newInstance(), "Vytvořit skupiny")
        viewPager.adapter = adapter
    }
}