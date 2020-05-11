package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ViewPagerAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.ranking.RankingFragment

/*
Represents an activity with view pager, where are stored fragments which server for displaying team ranking.
 */
class RankingViewPagerActivity : AbstractViewPagerActivity()
{
    private var group = Group()

    companion object
    {
        const val GROUP = "group"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.group) + " " + group.name)
    }

    override fun getData()
    {
        intent.extras?.let { bundle ->
            group = bundle.getParcelable(GROUP)!!
        }
    }

    override fun setupViewPager(viewPager: ViewPager)
    {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        group.teamIds.keys.sorted().forEach {
            adapter.addFragment(RankingFragment.newInstance(group, it.toInt()), it)
        }

        viewPager.adapter = adapter
        viewPager.setCurrentItem(group.teamIds.keys.count() - 1, true)
    }
}
