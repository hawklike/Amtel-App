package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowGroupsBossAdapter

abstract class AbstractActivityWithRecyclerView : AbstractBaseActivity()
{
    protected var recyclerView: RecyclerView? = null
    protected var adapter: ShowGroupsBossAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setupRecycler()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        recyclerView = null
        adapter = null
    }

    protected abstract fun setupRecycler()
}