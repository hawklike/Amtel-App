package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cz.prague.cvut.fit.steuejan.amtelapp.R
import kotlinx.android.synthetic.main.toolbar.*

abstract class AbstractBaseActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?)
    {
        super.onCreate(savedInstanceState, persistentState)
        setSupportActionBar(toolbar)
    }

    fun setToolbarTitle(title: String)
    {
        supportActionBar?.setDisplayShowTitleEnabled(false).also {
            val textView = toolbar.findViewById<TextView>(R.id.toolbar_title)
            textView.text = title
        }
    }

    fun setArrowBack(onClick: () -> Unit = { onBackPressed() })
    {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp)
        toolbar.setNavigationOnClickListener {
            onClick()
        }
    }

    companion object
    {
        const val TAG = "amtel_app_log"
    }

}