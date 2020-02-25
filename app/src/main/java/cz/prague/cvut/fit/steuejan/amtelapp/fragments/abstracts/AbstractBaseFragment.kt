package cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts

import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class AbstractBaseFragment : Fragment(), CoroutineScope
{
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job + handler

    protected open val job: Job = Job()

    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.e("CoroutineScope", "$exception handled !")
    }

    abstract fun getName(): String

    companion object
    {
        const val TAG = "amtel_app_log"
    }
}