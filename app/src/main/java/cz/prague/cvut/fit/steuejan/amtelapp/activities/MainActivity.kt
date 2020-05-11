package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.TestingUtil
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.account.AccountFragment
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.account.LoginFragment
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.menu.ShowGroupsMenuFragment
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.miscellaneous.PlayersFragment
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.miscellaneous.ReportsFragment
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.miscellaneous.RulesFragment
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.miscellaneous.TeamsFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.DeletedUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities.MainActivityVM
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : AbstractBaseActivity()
{
    private val viewModel by viewModels<MainActivityVM>()

    private lateinit var drawer: Drawer
    private var progressLayout: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.activity_main)
        progressLayout = findViewById(R.id.progressBar)
        super.onCreate(savedInstanceState)
        toolbar.setTitleTextColor(App.getColor(R.color.blue))
        viewModel.checkInternetConnection()
        viewModel.getActualSeason()
        viewModel.initEmailPassword()
        viewModel.initHeadOfLeague()
        setObservers(savedInstanceState)
        createNavigationDrawer(savedInstanceState)
    }

    override fun onDestroy()
    {
        progressLayout?.removeAllViews()
        progressLayout = null
        DateUtil.serverTime = null
        super.onDestroy()
    }

    private fun setObservers(savedInstanceState: Bundle?)
    {
        setToolbarTitle()
        displayAccount(savedInstanceState)
        updateDrawer()
        showProgressBar()
        checkInternetConnection()
        userAccountDeleted()
    }

    private fun checkInternetConnection()
    {
        viewModel.connection.observe(this) { hasInternet ->
            if(!hasInternet)
            {
                MaterialDialog(this)
                    .title(text = getString(R.string.bad_internet_connection_title))
                    .message(text = getString(R.string.bad_internet_connection_message))
                    .positiveButton()
                    .show()
            }
        }
    }

    private fun showProgressBar()
    {
        viewModel.progressBar.observe(this) { isOn ->
            if(isOn) progressLayout?.visibility = View.VISIBLE
            else progressLayout?.visibility = View.GONE
        }
    }

    private fun setToolbarTitle()
    {
        viewModel.getTitle().observe(this) { title ->
            setToolbarTitle(title)
        }
    }

    private fun displayAccount(savedInstanceState: Bundle?)
    {
        //the user is logged in and MainActivity is shown for the first time
        AuthManager.currentUser?.let { firebaseUser ->
            if(savedInstanceState == null)
                viewModel.prepareUser(firebaseUser.uid)
        }

        //invoked when a user logged in state changes
        viewModel.isUserLoggedIn().observe(this) { user ->
            when(user)
            {
                is SignedUser -> {
                    Log.d(TAG, "${user.self} is signed")
                    TestingUtil.setCurrentUser(user.self.email)
                    //change the item name in the navigation drawer
                    if(::drawer.isInitialized) drawer.updateName(0, StringHolder(getString(R.string.account)))
                    baseActivityVM.setLogoutIcon(true)
                    //show the account fragment
                    populateFragment(AccountFragment.newInstance())
                }
                is DeletedUser -> userDeleted(0)
                else -> {
                    //user is unsigned
                    Log.d(TAG, "user unsigned")
                    //change the item name in the navigation drawer
                    if(::drawer.isInitialized) drawer.updateName(0, StringHolder(getString(R.string.login)))
                    //show the login fragment
                    populateFragment(LoginFragment.newInstance())
                }
            }
        }
    }

    private fun userAccountDeleted()
    {
        //invoked when the user was deleted
        viewModel.userAccountDeleted.observe(this) { deleted ->
            if(deleted) showUserDeletedDialog { userDeleted(2) }
        }
    }

    private fun showUserDeletedDialog(onDismiss: () -> Unit)
    {
        MaterialDialog(this).show {
            title(text = getString(R.string.invalid_account))
            message(text = getString(R.string.account_deleted_text))
            positiveButton()
            onDismiss { onDismiss.invoke() }
        }
    }

    private fun createNavigationDrawer(savedInstanceState: Bundle?)
    {
        val profileTitle = AuthManager.profileDrawerOptionMenu
        val profile = PrimaryDrawerItem().withIdentifier(0).withName(profileTitle).withIcon(FontAwesome.Icon.faw_user_edit)
        val results = PrimaryDrawerItem().withIdentifier(1).withName(getString(R.string.results)).withIcon(FontAwesome.Icon.faw_list_ol)
        val schedule = PrimaryDrawerItem().withIdentifier(2).withName(getString(R.string.schedule)).withIcon(FontAwesome.Icon.faw_calendar_alt)
        val teams = SecondaryDrawerItem().withIdentifier(3).withName(getString(R.string.teams)).withIcon(FontAwesome.Icon.faw_users)
        val players = SecondaryDrawerItem().withIdentifier(4).withName(getString(R.string.players)).withIcon(FontAwesome.Icon.faw_user)
        val rules = SecondaryDrawerItem().withIdentifier(5).withName(getString(R.string.rules)).withIcon(FontAwesome.Icon.faw_connectdevelop)
        val reports = SecondaryDrawerItem().withIdentifier(6).withName(getString(R.string.reports)).withIcon(FontAwesome.Icon.faw_newspaper)

        drawer = DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .withHeader(R.layout.drawer_header)
            .withHeaderDivider(false)
            .withTranslucentStatusBar(false)
            .addDrawerItems(
                profile,
                results,
                schedule,
                DividerDrawerItem(),
                teams,
                players,
                rules,
                reports
            )
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener
            {
                //handles which fragment to display when clicked on an item in the navigation drawer
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean
                {
                    viewModel.setDrawerSelectedPosition(position)
                    when(drawerItem)
                    {
                        profile -> AuthManager.currentUser?.let {
                            val user = viewModel.isUserLoggedIn().value
                            if(user is SignedUser) populateFragment(AccountFragment.newInstance())
                        } ?: populateFragment(LoginFragment.newInstance())
                        results -> populateFragment(ShowGroupsMenuFragment.newInstance(true))
                        schedule -> populateFragment(ShowGroupsMenuFragment.newInstance(false))
                        teams -> populateFragment(TeamsFragment.newInstance())
                        players -> populateFragment(PlayersFragment.newInstance())
                        rules  -> populateFragment(RulesFragment.newInstance())
                        reports -> populateFragment(ReportsFragment.newInstance())
                    }
                    return false
                }
            }).build()

        //highlight selected item and remember the item
        if(savedInstanceState == null)
        {
            AuthManager.currentUser?.let { drawer.setSelection(profile) }
                ?: drawer.setSelection(schedule)
            viewModel.setDrawerSelectedPosition(drawer.currentSelectedPosition)
        }
    }

    private fun updateDrawer()
    {
        //in order to still show selected item when screen rotates
        viewModel.getDrawerSelectedPosition().observe(this) {
            if(::drawer.isInitialized) drawer.setSelectionAtPosition(it, false)
        }
    }

    /*
    User is still saved in the database, but lost a role which gives him an access.
    Therefore has to be logged out.
     */
    private fun userDeleted(menuItem: Long)
    {
        logout()
        drawer.setSelection(menuItem, true)
        viewModel.setDrawerSelectedPosition(drawer.currentSelectedPosition)
    }

    /*
    Shows a fragment given as an argument.
     */
    private fun populateFragment(fragment: AbstractMainActivityFragment)
    {
        Log.d(TAG, "${fragment.getName()} populated")
        supportFragmentManager.commit {
            replace(R.id.main_container, fragment)
        }
    }
}
