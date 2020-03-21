package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowGroupsBossAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.callbacks.ItemMoveCallback
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.account.AccountBossMakeGroupsFragment.Companion.GROUPS
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ManageGroupsActivityVM


class ManageGroupsActivity : AbstractActivityWithRecyclerView()
{
    private val viewModel by viewModels<ManageGroupsActivityVM>()

    private lateinit var weekLayout: TextInputLayout
    private lateinit var setWeek: FloatingActionButton

    private lateinit var progressBar: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.account_boss_groups)
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.groups))
        setArrowBack()
        initViews()
        getGroups()
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
        progressBar = findViewById(R.id.account_boss_groups_progressBar)
    }

    private fun getGroups()
    {
        progressBar.visibility = VISIBLE
        viewModel.getGroupsExceptPlayOff()
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
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        viewModel.groups.observe(this) {
            adapter = ShowGroupsBossAdapter(this, it)

            val callback: ItemTouchHelper.Callback = ItemMoveCallback(adapter!!)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(recyclerView)

            progressBar.visibility = GONE
            recyclerView?.adapter = adapter
        }

    }

    override fun onBackPressed()
    {
        adapter?.list?.let {
            val intent = Intent().apply {
                putParcelableArrayListExtra(GROUPS, ArrayList(it))
            }
            setResult(Activity.RESULT_OK, intent)
        }
        finish()
    }
}