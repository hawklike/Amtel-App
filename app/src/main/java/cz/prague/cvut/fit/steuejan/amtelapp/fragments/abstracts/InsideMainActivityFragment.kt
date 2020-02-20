package cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import cz.prague.cvut.fit.steuejan.amtelapp.activities.MainActivity
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MainActivityVM

abstract class InsideMainActivityFragment : AbstractBaseFragment()
{
    protected val mainActivityModel by activityViewModels<MainActivityVM>()

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setProgressBar(false)
    }

    protected open fun setProgressBar(on: Boolean)
    {
        val main = activity as MainActivity
        if(on) main.progressLayout?.visibility = View.VISIBLE
        else main.progressLayout?.visibility = View.INVISIBLE
    }

    protected open fun setToolbarTitle(title: String)
    {
        mainActivityModel.setTitle(title)
    }
}