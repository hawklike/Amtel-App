package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowGroupsBossAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.callbacks.ItemMoveCallback
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.account.AccountBossMakeGroupsFragment.Companion.GROUPS
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ManageGroupsActivityVM
import java.util.*
import kotlin.collections.ArrayList


class ManageGroupsActivity : AbstractActivityWithRecyclerView()
{
    private val viewModel by viewModels<ManageGroupsActivityVM>()

    private lateinit var progressBar: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.account_boss_groups)
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.groups))
        setArrowBack()
        initViews()
        getGroups()
        setPlayOff()
        updateFields()
        setListeners()
        setObservers()
    }

    private fun setPlayOff()
    {
        val switch = findViewById<Switch>(R.id.account_boss_groups_open_playOff_switch)
        switch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked)
            {
                MaterialDialog(this)
                    .title(text = "Opravdu chcete otevřít baráž?")
                    .show {
                        positiveButton(R.string.ok) { viewModel.setPlayOff(DateUtil.getWeekNumber(Date())) }
                        negativeButton()
                    }
            }
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }

    private fun initViews()
    {
        progressBar = findViewById(R.id.account_boss_groups_progressBar)
        setPlayOffInterval()
    }

    @SuppressLint("SetTextI18n")
    private fun setPlayOffInterval()
    {
        val playOffInterval = findViewById<TextView>(R.id.account_boss_groups_open_playOff_text)
        playOffInterval.text = "${Date().toMyString()} – ${DateUtil.getDateInFuture(14).toMyString()}"
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
//        setWeek.setOnClickListener {
//            val week = weekLayout.editText?.text.toString().trim()
//            weekLayout.error = null
//            viewModel.setPlayOff(week)
//        }
    }

    private fun setObservers()
    {
//        viewModel.week.observe(this) { week ->
//           if(week is InvalidWeek) weekLayout.error = week.errorMessage
//            else weekLayout.editText?.setText((week as ValidWeek).self.toString())
//        }
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