package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.viewpager.widget.ViewPager
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ViewPagerAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.schedule.ScheduleRoundFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.NoUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.UserState
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ScheduleRoundsActivityVM

class ScheduleRoundsActivity : AbstractViewPagerActivity()
{
    private val viewModel by viewModels<ScheduleRoundsActivityVM>()

    private var group = Group()
    private var user: UserState = NoUser

    companion object
    {
        const val GROUP = "group"
        const val USER = "user"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        viewModel.setUser(user)
        setToolbarTitle(getString(R.string.group) + " " + group.name)
    }

    override fun getData()
    {
        intent.extras?.let { bundle ->
            group = bundle.getParcelable(GROUP)!!
            user = bundle.getParcelable<User?>(USER)?.let { SignedUser(it) } ?: NoUser
        }
    }

    override fun setupViewPager(viewPager: ViewPager)
    {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        (1..group.rounds).map { round ->
            adapter.addFragment(
                ScheduleRoundFragment.newInstance(round, group),
                round.toString() + ". " + getString(R.string.round)
            )
        }
        viewPager.adapter = adapter
    }
}