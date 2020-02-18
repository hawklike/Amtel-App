package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ViewPagerAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.schedule.ScheduleRoundFragment

class ScheduleRoundsActivity : AbstractBaseActivity()
{
    private lateinit var viewPager: ViewPager
    private lateinit var tabs: TabLayout

    private lateinit var group: Group

    companion object
    {
        const val GROUP = "group"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.schedule_rounds_menu)
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.rounds))
        setArrowBack()

        intent.extras?.let { group = it.getParcelable(GROUP)!! }

        viewPager = findViewById(R.id.schedule_rounds_menu_viewPager)
        tabs = findViewById(R.id.schedule_rounds_menu_tabs)

        setupViewPager(viewPager, group)
        tabs.setupWithViewPager(viewPager)
    }

    override fun onBackPressed()
    {
        super.onBackPressed()
        finish()
    }

    private fun setupViewPager(viewPager: ViewPager, group: Group)
    {
        val adapter = ViewPagerAdapter(supportFragmentManager)

        (1..group.rounds).map {
            adapter.addFragment(
                ScheduleRoundFragment.newInstance(group),
                it.toString() + "." + getString(R.string.round)
            )
        }

        viewPager.adapter = adapter
    }
}