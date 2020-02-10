package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowGroupsFirestoreAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group

class ShowGroupsActivity : AbstractActivityWithRecyclerView()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.account_boss_groups)
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.groups))
    }

    override fun setupRecycler()
    {
        recyclerView = findViewById(R.id.account_boss_groups_recyclerView)

        val query = GroupManager.retrieveAllGroups("name")
        val options = FirestoreRecyclerOptions.Builder<Group>()
            .setQuery(query, Group::class.java)
            .build()

        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = ShowGroupsFirestoreAdapter(this, options)
        recyclerView?.adapter = adapter
    }
}