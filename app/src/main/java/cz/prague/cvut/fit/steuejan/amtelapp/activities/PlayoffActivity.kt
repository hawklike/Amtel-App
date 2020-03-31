package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.commit
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toPlayoff
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.schedule.ScheduleRoundFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.NoUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.PlayoffActivityVM

class PlayoffActivity : AbstractBaseActivity()
{
    companion object
    {
        const val GROUP = "group"
        const val USER = "user"
    }

    private val viewModel by viewModels<PlayoffActivityVM>()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.playoff)
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.playOff))
        setArrowBack()

        intent.extras?.let { bundle ->
            val group = bundle.getParcelable<Group>(GROUP)
            val user = bundle.getParcelable<User?>(USER)

            if(user != null) viewModel.setUser(SignedUser(user))
            else viewModel.setUser(NoUser)

            viewModel.setPlayoff(group?.toPlayoff())

            group?.let {
                supportFragmentManager.commit {
                    replace(R.id.playoff_container, ScheduleRoundFragment.newInstance(1, it, true))
                }
            }
        }
    }
}