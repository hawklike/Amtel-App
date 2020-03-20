package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowGroupsBossFirestoreAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ManageGroupsActivityVM

class ManageGroupsActivity : AbstractActivityWithRecyclerView()
{
    private val viewModel by viewModels<ManageGroupsActivityVM>()

    private lateinit var weekLayout: TextInputLayout
    private lateinit var setWeek: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.account_boss_groups)
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.groups))
        setArrowBack()
        initViews()
        updateFields()
        setListeners()
        setObservers()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        setWeek.setOnClickListener(null)
    }

    private fun initViews()
    {
        weekLayout = findViewById(R.id.account_boss_groups_choose_week_week)
        setWeek = findViewById(R.id.account_boss_groups_choose_week_add)
    }

    private fun updateFields()
    {
        viewModel.getPlayOffWeek()
    }

    private fun setListeners()
    {
        setWeek.setOnClickListener {
            val week = weekLayout.editText?.text.toString().trim()
            weekLayout.error = null
            viewModel.setPlayOff(week)
        }
    }

    private fun setObservers()
    {
        viewModel.week.observe(this) { week ->
           if(week is InvalidWeek) weekLayout.error = week.errorMessage
            else weekLayout.editText?.setText((week as ValidWeek).self.toString())
        }
    }

    override fun setupRecycler()
    {
        recyclerView = findViewById(R.id.account_boss_groups_recyclerView)

        val query = GroupManager.retrieveAllGroups("privateName")
        val options = FirestoreRecyclerOptions.Builder<Group>()
            .setQuery(query, Group::class.java)
            .build()

        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = ShowGroupsBossFirestoreAdapter(this, options)
        recyclerView?.adapter = adapter
    }
}