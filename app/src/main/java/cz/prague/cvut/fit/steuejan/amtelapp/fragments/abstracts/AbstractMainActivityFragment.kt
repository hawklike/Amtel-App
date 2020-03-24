package cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MainActivityVM

abstract class AbstractMainActivityFragment : AbstractBaseFragment()
{
    protected val mainActivityModel by activityViewModels<MainActivityVM>()

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setProgressBar(false)
        activity!!.window.navigationBarColor = App.getColor(R.color.veryVeryLightGrey)
    }

    protected open fun setProgressBar(on: Boolean)
    {
        mainActivityModel.setProgressBar(on)
    }

    protected open fun setToolbarTitle(title: String)
    {
        mainActivityModel.setTitle(title)
    }
}