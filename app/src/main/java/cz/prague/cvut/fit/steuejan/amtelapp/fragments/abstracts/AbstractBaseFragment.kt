package cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import cz.prague.cvut.fit.steuejan.amtelapp.App
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

    fun toast(text: String, context: Context = App.context, length: Int = Toast.LENGTH_SHORT)
    {
        Toast.makeText(context, text, length).show()
    }

    abstract fun getName(): String

    companion object
    {
        const val TAG = "amtel_app_log"
    }
}