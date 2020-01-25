package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import cz.prague.cvut.fit.steuejan.amtelapp.activities.MainActivity
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MainActivityVM

abstract class AbstractBaseFragment : Fragment()
{
    protected val mainActivityModel by activityViewModels<MainActivityVM>()

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        val main = activity as MainActivity
        main.progressLayout.visibility = View.INVISIBLE
    }


    protected open fun setToolbarTitle(title: String)
    {
        mainActivityModel.setTitle(title)
    }

    abstract fun getName(): String

    companion object
    {
        const val TAG = "amtel_app_log"
    }

}