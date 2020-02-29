package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.ShowGroupsActivity
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowTeamsFirestoreAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.TeamOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidName
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.AccountBossMakeGroupsFragmentVM

class AccountBossMakeGroupsFragment : AbstractMainActivityFragment()
{
    companion object
    {
        fun newInstance(): AccountBossMakeGroupsFragment = AccountBossMakeGroupsFragment()
    }

    private val viewModel by viewModels<AccountBossMakeGroupsFragmentVM>()

    private lateinit var nameLayout: TextInputLayout
    private lateinit var createGroup: FloatingActionButton

    private lateinit var showGroups: RelativeLayout

    private var recyclerView: RecyclerView? = null
    private var adapter: ShowTeamsFirestoreAdapter? = null

    override fun getName(): String = "AccountBossMakeGroupsFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.account_boss_create_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        nameLayout = view.findViewById(R.id.account_boss_create_group_name)
        createGroup = view.findViewById(R.id.account_boss_create_group_add)
        showGroups = view.findViewById(R.id.account_boss_create_group_show_groups)
        recyclerView = view.findViewById(R.id.account_boss_create_group_recyclerView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setupRecycler()
        setListeners()
        setObservers()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        adapter = null
        recyclerView = null
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
        val query = TeamManager.retrieveAllTeams(TeamOrderBy.GROUP)
        val options = FirestoreRecyclerOptions.Builder<Team>()
            .setQuery(query, Team::class.java)
            .build()

        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = ShowTeamsFirestoreAdapter(activity!!, options)
        recyclerView?.adapter = adapter
    }

    private fun setListeners()
    {
        createGroup.setOnClickListener {
            val groupName = nameLayout.editText?.text.toString()

            nameLayout.error = null
            viewModel.createGroup(groupName)
        }

        showGroups.setOnClickListener {
            val intent = Intent(activity!!, ShowGroupsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setObservers()
    {
        getAllGroups()
        confirmGroupName()
        isGroupCreated()
    }

    private fun getAllGroups()
    {
        viewModel.getGroups()
        viewModel.getAllGroups().observe(viewLifecycleOwner) {
            adapter?.groups = it.map { group -> group.name }.sorted()
        }
    }

    private fun isGroupCreated()
    {
        viewModel.group.observe(viewLifecycleOwner) {
            val title = viewModel.displayDialog(it).title

            viewModel.updateGroups(it)

            MaterialDialog(activity!!)
                .title(text = title)
                .show {
                    positiveButton(R.string.ok)
                    onDismiss {}
                }

            nameLayout.editText?.text?.clear()
        }
    }

    private fun confirmGroupName()
    {
        viewModel.groupName.observe(viewLifecycleOwner) {
            if(it is InvalidName)
                nameLayout.error = it.errorMessage
        }
    }

}