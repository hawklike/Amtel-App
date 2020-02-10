package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowGroupsFirestoreAdapter

abstract class AbstractActivityWithRecyclerView : AbstractBaseActivity()
{
    protected var recyclerView: RecyclerView? = null
    protected var adapter: ShowGroupsFirestoreAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setArrowBack()
        setupRecycler()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        recyclerView = null
        adapter = null
    }

    override fun onStart()
    {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop()
    {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onBackPressed()
    {
        super.onBackPressed()
        finish()
    }

    protected abstract fun setupRecycler()
}