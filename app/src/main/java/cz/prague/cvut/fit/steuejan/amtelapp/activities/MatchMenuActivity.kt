package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ViewPagerAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.match.MatchViewFactory
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MatchMenuActivityVM

class MatchMenuActivity : AbstractViewPagerActivity()
{
    private val viewModel by viewModels<MatchMenuActivityVM>()

    private lateinit var match: Match
    private lateinit var week: WeekState

    private lateinit var title: String

    private var homeTeam: TeamState = NoTeam
    private var awayTeam: TeamState = NoTeam

    companion object
    {
        const val MATCH = "match"
        const val WEEK = "week"
        const val TITLE = "title"
        const val HOME_TEAM = "home"
        const val AWAY_TEAM = "away"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        viewModel.setMatch(match)
        viewModel.setWeek(week)
        viewModel.setHomeTeam(homeTeam)
        viewModel.setAwayTeam(awayTeam)
        setToolbarTitle(title)
    }

    override fun getData()
    {
        intent.extras?.let { bundle ->
            match = bundle.getParcelable(MATCH)!!
            week = bundle.getParcelable<ValidWeek?>(WEEK)?.let { it } ?: InvalidWeek()
            title = bundle.getString(TITLE)!!
            homeTeam = bundle.getParcelable<Team>(HOME_TEAM)?.let { ValidTeam(it) } ?: NoTeam
            awayTeam = bundle.getParcelable<Team>(AWAY_TEAM)?.let { ValidTeam(it) } ?: NoTeam
        }
    }

    override fun setupViewPager(viewPager: ViewPager)
    {
        tabs.tabMode = TabLayout.MODE_FIXED
        tabs.tabGravity = TabLayout.GRAVITY_FILL
        tabs.layoutParams.width= ViewGroup.LayoutParams.MATCH_PARENT

        val adapter = ViewPagerAdapter(supportFragmentManager)
        (1..3).map {
            adapter.addFragment(MatchViewFactory.getFragment(title, it), "$it. " + getString(R.string.match))
        }
        viewPager.adapter = adapter
    }
}