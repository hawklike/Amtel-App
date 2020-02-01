package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import cz.prague.cvut.fit.steuejan.amtelapp.activities.MainActivity
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MainActivityVM
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class AbstractBaseFragment : Fragment(), CoroutineScope
{
    protected val mainActivityModel by activityViewModels<MainActivityVM>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job + handler

    protected open val job: Job = Job()

    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.e("CoroutineScope", "$exception handled !")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        val main = activity as MainActivity
        main.progressLayout.visibility = View.INVISIBLE
    }

    protected fun setProgressBar(on: Boolean)
    {
        val main = activity as MainActivity
        if(on) main.progressLayout.visibility = View.VISIBLE
        else main.progressLayout.visibility = View.INVISIBLE
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