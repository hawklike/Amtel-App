package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ViewPagerAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.schedule.ScheduleRoundFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.NoUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.UserState
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ScheduleRoundsActivityVM

class ScheduleRoundsActivity : AbstractBaseActivity()
{
    private val viewModel by viewModels<ScheduleRoundsActivityVM>()

    private var group = Group()
    private var user: UserState = NoUser

    private lateinit var viewPager: ViewPager
    private lateinit var tabs: TabLayout

    private var chooseWeekLayout: RelativeLayout? = null

    private lateinit var weekLayout: TextInputLayout
    private lateinit var setWeek: FloatingActionButton

    companion object
    {
        const val GROUP = "group"
        const val USER = "user"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.schedule_rounds_menu)
        super.onCreate(savedInstanceState)
        setArrowBack()

        intent.extras?.let { bundle ->
            group = bundle.getParcelable(GROUP)!!
            user = bundle.getParcelable<User?>(USER)?.let { SignedUser(it) } ?: NoUser
        }

        viewModel.setUser(user)

        setToolbarTitle(getString(R.string.group) + " " + group.name)

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

        (1..group.rounds).map { round ->
            adapter.addFragment(
                ScheduleRoundFragment.newInstance(round, group.name),
                round.toString() + ". " + getString(R.string.round)
            )
        }

        viewPager.adapter = adapter
    }
}
