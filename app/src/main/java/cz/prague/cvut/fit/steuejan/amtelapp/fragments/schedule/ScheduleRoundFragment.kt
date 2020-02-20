package cz.prague.cvut.fit.steuejan.amtelapp.fragments.schedule

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowMatchesFirestoreAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.InsideScheduleActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.UserState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ScheduleRoundFragmentVM

class ScheduleRoundFragment : InsideScheduleActivityFragment()
{
    private val viewModel by viewModels<ScheduleRoundFragmentVM>()

    private var round = 0
    private lateinit var group: Group
    private lateinit var user: UserState

    private var chooseWeekLayout: RelativeLayout? = null

    private lateinit var weekRange: TextView
    private lateinit var weekLayout: TextInputLayout
    private lateinit var setWeek: FloatingActionButton

    private var recyclerView: RecyclerView? = null
    private var adapter: ShowMatchesFirestoreAdapter? = null

    companion object
    {
        private const val ROUND = "round"
        private const val GROUP = "group"

        fun newInstance(round: Int, group: Group): ScheduleRoundFragment
        {
            val fragment = ScheduleRoundFragment()
            fragment.arguments = Bundle().apply {
                putInt(ROUND, round)
                putParcelable(GROUP, group)
            }
            return fragment
        }
    }

    override fun getName(): String = "ScheduleRoundFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        arguments?.getInt(ROUND)?.let { round = it }
        arguments?.getParcelable<Group>(GROUP)?.let { group = it }
        return inflater.inflate(R.layout.schedule_round, container, false)
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

    override fun onDestroyView()
    {
        super.onDestroyView()
        recyclerView = null
        chooseWeekLayout?.removeAllViews()
        chooseWeekLayout = null
    }

    override fun onDestroy()
    {
        super.onDestroy()
        adapter = null
        setWeek.setOnClickListener(null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.schedule_round_recyclerView)
        weekRange = view.findViewById(R.id.schedule_round_week_text)
        chooseWeekLayout = view.findViewById(R.id.schedule_round_choose_week)
        weekLayout = view.findViewById(R.id.schedule_round_choose_week_week)
        setWeek = view.findViewById(R.id.schedule_round_choose_week_add)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        user = scheduleViewModel.getUser().value!!

        if(user is SignedUser && (user as SignedUser).self.role.toRole() == UserRole.HEAD_OF_LEAGUE)
            chooseWeekLayout?.visibility = View.VISIBLE

        setupRecycler()
        getWeek()
        setListeners()
        setObservers()
    }

    private fun getWeek()
    {
        viewModel.getWeek(group, round)

    }

    private fun setListeners()
    {
        setWeek.setOnClickListener {
            val week = weekLayout.editText?.text.toString().trim()
            weekLayout.error = null
            viewModel.addWeek(week, group, round)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setObservers()
    {
        viewModel.week.observe(viewLifecycleOwner) { week ->
            when(week)
            {
                is InvalidWeek -> weekLayout.error = week.errorMessage
                is ValidWeek -> {
                    weekRange.visibility = View.VISIBLE
                    weekRange.text = "${week.range.first.toMyString()} - ${week.range.second.toMyString()}"
                    weekLayout.editText?.setText(week.self.toString())
                }
            }
        }
    }

    private fun setupRecycler()
    {
        val query = MatchManager.getMatches(round, group.name)
        val options = FirestoreRecyclerOptions.Builder<Match>()
            .setQuery(query, Match::class.java)
            .build()

        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = ShowMatchesFirestoreAdapter(user, options)

        adapter?.onNextClickOwner = { match ->
            toast(match.home)
        }

        adapter?.onNextClickGuest = { match ->
            toast(match.away)
        }

        recyclerView?.adapter = adapter
    }
}