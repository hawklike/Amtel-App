package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.*
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MainActivityVM
import kotlinx.android.synthetic.main.toolbar.*


class MainActivity : AbstractBaseActivity()
{
    private val viewModel by viewModels<MainActivityVM>()
    private lateinit var drawer: Drawer

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        setObservers(savedInstanceState)
        createNavigationDrawer(savedInstanceState)
    }

    private fun setObservers(savedInstanceState: Bundle?)
    {
        setToolbarTitle()
        displayAccount(savedInstanceState)
    }

    private fun setToolbarTitle()
    {
        viewModel.getTitle().observe(this) { title ->
            setToolbarTitle(title)
        }
    }

    private fun displayAccount(savedInstanceState: Bundle?)
    {
        //TODO: change to a User
        AuthManager.getCurrentUser()?.let { user ->
            savedInstanceState ?: viewModel.setUser(MainActivityVM.UserStatus.SignedUser(user))
        }

        viewModel.getUser().observe(this) { user ->
            if(user is MainActivityVM.UserStatus.SignedUser)
            {
                if(::drawer.isInitialized)
                    drawer.updateName(0, StringHolder(getString(R.string.account)))
                logoutIcon.visibility = View.VISIBLE
                populateFragment(AccountFragment.newInstance(user.self))
            }
            else
            {
                if(::drawer.isInitialized)
                    drawer.updateName(0, StringHolder(getString(R.string.login)))
                populateFragment(LoginFragment.newInstance())
            }
        }
    }

    private fun createNavigationDrawer(savedInstanceState: Bundle?)
    {
        val profileTitle = AuthManager.getProfileDrawerOption(applicationContext)
        val profile = PrimaryDrawerItem().withIdentifier(0).withName(profileTitle).withIcon(FontAwesome.Icon.faw_user_edit)
        val results = PrimaryDrawerItem().withName(getString(R.string.results)).withIcon(FontAwesome.Icon.faw_list_ol)
        val schedule = PrimaryDrawerItem().withName(getString(R.string.schedule)).withIcon(FontAwesome.Icon.faw_calendar_alt)
        val teams = PrimaryDrawerItem().withName(getString(R.string.teams)).withIcon(FontAwesome.Icon.faw_users)
        val players = PrimaryDrawerItem().withName(getString(R.string.players)).withIcon(FontAwesome.Icon.faw_user)

        drawer = DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .withTranslucentStatusBar(false)
            .addDrawerItems(
                profile,
                results,
                schedule,
                teams,
                players
            )
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener
            {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean
                {
                    viewModel.setDrawerSelectedPosition(position)
                    when(drawerItem)
                    {
                        profile -> AuthManager.getCurrentUser()?.let {
                            val user = viewModel.getUser().value
                            if(user is MainActivityVM.UserStatus.SignedUser)
                                populateFragment(AccountFragment.newInstance(user.self))
                        } ?: populateFragment(LoginFragment.newInstance())
                        results -> populateFragment(ResultsFragment.newInstance())
                        schedule -> populateFragment(ScheduleFragment.newInstance())
                        teams -> populateFragment(TeamsFragment.newInstance())
                        players -> populateFragment(PlayersFragment.newInstance())
                    }
                    return false
                }
            }).build()

        drawer.drawerLayout.setStatusBarBackground(R.color.white)
        //restores option before configuration change (i.e rotation...)
        drawer.setSelectionAtPosition(viewModel.getDrawerSelectedPosition(), false)

        if(savedInstanceState == null)
        {
            drawer.setSelection(profile)
            viewModel.setDrawerSelectedPosition(drawer.currentSelectedPosition)
        }
    }

    private fun populateFragment(fragment: Fragment)
    {
        supportFragmentManager.commit {
            replace(R.id.main_container, fragment)
        }
    }
}
