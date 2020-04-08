package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.callbacks.ItemMoveCallback
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal.ShowGroupsBossAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Playoff.Companion.PLAYOFF_DAYS
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.account.AccountBossMakeGroupsFragment.Companion.GROUPS
import cz.prague.cvut.fit.steuejan.amtelapp.services.SeasonFinisherService
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ManageGroupsActivityVM
import java.util.*
import kotlin.collections.ArrayList


class ManageGroupsActivity : AbstractBaseActivity()
{
    private val viewModel by viewModels<ManageGroupsActivityVM>()

    private var recyclerView: RecyclerView? = null
    private var adapter: ShowGroupsBossAdapter? = null

    private lateinit var generatePlayOffButton: Button
    private lateinit var progressBar: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.account_boss_groups)
        super.onCreate(savedInstanceState)
        setupRecycler()
        setToolbarTitle(getString(R.string.groups))
        setArrowBack()
        initViews()
        getGroups()
        getPlayoff()
        generatePlayoff()
    }

    private fun getPlayoff()
    {
        viewModel.playoff ?: viewModel.getPlayoff()
        viewModel.isPlayOffOpen.observe(this) { open ->
            generatePlayOffButton.visibility = VISIBLE
            if(open)
            {
                disablePlayoffButton()
                viewModel.playoff?.let { setPlayoffInterval(it.startDate, it.endDate) }
            }
            else setPlayoffInterval(Date(), DateUtil.getDateInFuture(PLAYOFF_DAYS - 1))
        }
    }

    private fun disablePlayoffButton()
    {
        generatePlayOffButton.setTextColor(App.getColor(R.color.red))
        generatePlayOffButton.isEnabled = false
        generatePlayOffButton.text = "Otevřeno"
    }

    private fun generatePlayoff()
    {
        generatePlayOffButton.setOnClickListener {
            MaterialDialog(this)
                .title(text = "Opravdu chcete otevřít baráž?")
                .message(text = "Budou vygenerována utkání o postup/sestup a přesunuty nejlepší/nejhorší týmy do patřičných skupin.\n\nBaráž bude otevřena na dva týdny, poté se automaticky uzavře a aktuální sezóna bude ukončena. Po uplynutí dvou týdnů budete moct otevřít novou baráž.\n\nMějte prosím strpení, bude to chvíli trvat.")
                .show {
                    positiveButton(text = "Otevřít") {
                        setPlayOff()
                        disablePlayoffButton()
                    }
                    negativeButton()
            }
        }
    }

    private fun setPlayOff()
    {
        val groups = viewModel.groups.value ?: emptyList()
        val serviceIntent = Intent(this, SeasonFinisherService::class.java).apply {
            putParcelableArrayListExtra(SeasonFinisherService.GROUPS_EXCEPT_PLAYOFF, ArrayList(groups))
        }
        startService(serviceIntent)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        generatePlayOffButton.setOnClickListener(null)
        recyclerView = null
        adapter = null
    }

    private fun initViews()
    {
        progressBar = findViewById(R.id.account_boss_groups_progressBar)
        generatePlayOffButton = findViewById(R.id.account_boss_groups_playOff_generate)
    }

    @SuppressLint("SetTextI18n")
    private fun setPlayoffInterval(startDate: Date, endDate: Date)
    {
        val playOffInterval = findViewById<TextView>(R.id.account_boss_groups_open_playOff_text)
        playOffInterval.text = "${startDate.toMyString()} – ${endDate.toMyString()}"
    }

    private fun getGroups()
    {
        progressBar.visibility = VISIBLE
        viewModel.getGroupsExceptPlayOff()
    }

    private fun setupRecycler()
    {
        recyclerView = findViewById(R.id.account_boss_groups_recyclerView)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        viewModel.groups.observe(this) {
            adapter = ShowGroupsBossAdapter(
                this,
                it.toMutableList()
            )

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