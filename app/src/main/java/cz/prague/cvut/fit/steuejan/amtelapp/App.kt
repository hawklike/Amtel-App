package cz.prague.cvut.fit.steuejan.amtelapp

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

class App : Application()
{
    override fun onCreate()
    {
        super.onCreate()
        mContext = WeakReference(this)
    }

    companion object
    {
        private var mContext: WeakReference<Context>? = null
        val context: Context
            get() = mContext?.get()!!

        fun toast(text: String, context: Context = this.context, length: Int = Toast.LENGTH_SHORT)
        {
            Toast.makeText(context, text, length).show()
        }

        fun getColor(@ColorRes color: Int): Int
        {
            return ContextCompat.getColor(context, color)
        }
    }
}