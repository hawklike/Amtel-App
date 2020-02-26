package cz.prague.cvut.fit.steuejan.amtelapp.fragments.schedule

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.ScheduleRoundsActivity
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowGroupsFirestoreAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment

class ScheduleGroupsMenuFragment : AbstractMainActivityFragment()
{
    companion object
    {
        fun newInstance(): ScheduleGroupsMenuFragment = ScheduleGroupsMenuFragment()
    }

    private var recyclerView: RecyclerView? = null
    private var adapter: ShowGroupsFirestoreAdapter? = null

    override fun getName(): String = "ScheduleFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.schedule_groups_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.schedule_groups_recyclerView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle(getString(R.string.schedule))
        setupRecycler()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        recyclerView = null
    }

    override fun onDestroy()
    {
        super.onDestroy()
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

    private fun setupRecycler()
    {
        val query = GroupManager.retrieveAllGroups("name")
        val options = FirestoreRecyclerOptions.Builder<Group>()
            .setQuery(query, Group::class.java)
            .build()

        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(activity!!)
        adapter = ShowGroupsFirestoreAdapter(activity!!, options)

        adapter?.setNextButton(true) { group ->
            val intent = Intent(activity!!, ScheduleRoundsActivity::class.java).apply {
                putExtra(ScheduleRoundsActivity.GROUP, group)
                putExtra(ScheduleRoundsActivity.USER, mainActivityModel.getUser().value)
            }

            if(group.rounds != 0) startActivity(intent)
            else Toast.makeText(activity!!, getString(R.string.no_rounds_text), Toast.LENGTH_SHORT).show()
        }
        recyclerView?.adapter = adapter
    }
}