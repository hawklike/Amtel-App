package cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.MainActivity
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MainActivityVM

abstract class AbstractMainActivityFragment : AbstractBaseFragment()
{
    protected val mainActivityModel by activityViewModels<MainActivityVM>()

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setProgressBar(false)
        activity?.window?.navigationBarColor = App.getColor(R.color.veryVeryLightGrey)
    }

    protected fun setLogoutIconVisibility(visible: Boolean)
    {
        val mainActivity = activity as? MainActivity
        mainActivity?.baseActivityVM?.setLogoutIcon(visible)
    }

    protected fun setProgressBar(on: Boolean)
    {
        mainActivityModel.setProgressBar(on)
    }

    protected fun setToolbarTitle(title: String)
    {
        mainActivityModel.setTitle(title)
    }
}