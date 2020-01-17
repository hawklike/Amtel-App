package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import cz.prague.cvut.fit.steuejan.amtelapp.models.MainActivityModel

abstract class AbstractBaseFragment : Fragment()
{
    private val mainActivityModel by activityViewModels<MainActivityModel>()

    protected open fun setToolbarTitle(title: String)
    {
        mainActivityModel.setTitle(title)
    }

    companion object
    {
        const val TAG = "amtel_app_log"
    }

}