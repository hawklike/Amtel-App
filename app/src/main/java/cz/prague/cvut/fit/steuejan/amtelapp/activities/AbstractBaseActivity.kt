package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager.auth
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.AbstractBaseActivityVM
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MainActivityVM
import kotlinx.android.synthetic.main.toolbar.*

abstract class AbstractBaseActivity : AppCompatActivity()
{
    private val logoutIcon: ImageView by lazy { findViewById<ImageView>(R.id.toolbar_logout) }
    protected val baseActivityVM by viewModels<AbstractBaseActivityVM>()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        handleLogout()
    }

    private fun handleLogout()
    {
        baseActivityVM.getLogoutIconVisibility().observe(this) { visibility ->
            if(visibility) logoutIcon.visibility = View.VISIBLE
            else logoutIcon.visibility = View.GONE
        }

        logoutIcon.setOnClickListener {
            displayLogoutDialog()
        }
    }

    private fun displayLogoutDialog()
    {
        MaterialDialog(this)
            .title(R.string.logout)
            .message(R.string.logout_message)
            .show{
                positiveButton(R.string.yes) { logout() }
                negativeButton(R.string.no)
            }
    }

    private fun logout()
    {
        Log.i(TAG, "logout")
        auth.signOut()
        val mainActivityModel by viewModels<MainActivityVM>()
        mainActivityModel.setTitle(getString(R.string.login))
        mainActivityModel.setUser(MainActivityVM.UserStatus.NoUser)
        baseActivityVM.setLogoutIconVisibility(false)
    }

    protected fun setToolbarTitle(title: String)
    {
        supportActionBar?.setDisplayShowTitleEnabled(false).also {
            val textView = toolbar.findViewById<TextView>(R.id.toolbar_title)
            textView.text = title
        }
    }

    protected fun setArrowBack(onClick: () -> Unit = { onBackPressed() })
    {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp)
        toolbar.setNavigationOnClickListener {
            onClick()
        }
    }

    companion object
    {
        const val TAG = "MainActivity"
    }

}