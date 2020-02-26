package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ViewPagerAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.match.MatchViewFactory
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MatchMenuActivityVM

class MatchMenuActivity : AbstractViewPagerActivity()
{
    private val viewModel by viewModels<MatchMenuActivityVM>()

    private lateinit var match: Match
    private lateinit var week: WeekState

    private lateinit var title: String

    companion object
    {
        const val MATCH = "match"
        const val WEEK = "week"
        const val TITLE = "title"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        viewModel.setMatch(match)
        viewModel.setWeek(week)
        setToolbarTitle(title)
    }

    override fun getData()
    {
        intent.extras?.let { bundle ->
            match = bundle.getParcelable(MATCH)!!
            week = bundle.getParcelable<ValidWeek?>(WEEK)?.let { it } ?: InvalidWeek()
            title = bundle.getString(TITLE)!!
        }
    }

    override fun setupViewPager(viewPager: ViewPager)
    {
        tabs.tabMode = TabLayout.MODE_FIXED
        tabs.tabGravity = TabLayout.GRAVITY_FILL
        tabs.layoutParams.width= ViewGroup.LayoutParams.MATCH_PARENT

        val adapter = ViewPagerAdapter(supportFragmentManager)
        (1..3).map {
            adapter.addFragment(MatchViewFactory.getFragment(title), "$it. zápas")
        }
        viewPager.adapter = adapter
    }
}