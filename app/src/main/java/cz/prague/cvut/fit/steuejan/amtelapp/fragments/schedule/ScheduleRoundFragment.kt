package cz.prague.cvut.fit.steuejan.amtelapp.fragments.schedule

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.MatchArrangementActivity
import cz.prague.cvut.fit.steuejan.amtelapp.activities.MatchViewPagerActivity
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.realtime.ShowMatchesFirestoreAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.MatchRepository
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Playoff
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractScheduleActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.UserState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities.PlayoffActivityVM
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments.ScheduleRoundFragmentVM

class ScheduleRoundFragment : AbstractScheduleActivityFragment()
{
    private val viewModel by viewModels<ScheduleRoundFragmentVM>()
    private val playoffViewModel by activityViewModels<PlayoffActivityVM>()

    private var round = 0
    private lateinit var group: Group
    private lateinit var user: UserState
    private lateinit var playoff: Playoff
    private var isPlayoff = false

    private val week: ValidWeek? by lazy { viewModel.week.value?.let { week ->
        if(week is ValidWeek) week else null  }
    }

    private var chooseWeekLayout: RelativeLayout? = null

    private lateinit var title: TextView
    private lateinit var weekRange: TextView
    private lateinit var deadline: TextView
    private lateinit var weekLayout: TextInputLayout
    private lateinit var setWeek: FloatingActionButton

    private var recyclerView: RecyclerView? = null
    private var adapter: ShowMatchesFirestoreAdapter? = null

    companion object
    {
        private const val ROUND = "round"
        private const val GROUP = "group"
        private const val PLAYOFF = "isPlayoff"

        fun newInstance(round: Int, group: Group, playoff: Boolean = false): ScheduleRoundFragment
        {
            val fragment = ScheduleRoundFragment()
            fragment.arguments = Bundle().apply {
                putInt(ROUND, round)
                putParcelable(GROUP, group)
                putBoolean(PLAYOFF, playoff)
            }
            return fragment
        }
    }

    override fun getName(): String = "ScheduleRoundFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        arguments?.run {
            round = getInt(ROUND)
            getParcelable<Group>(GROUP)?.let { group = it }
            isPlayoff = getBoolean(PLAYOFF)
        }
        return inflater.inflate(R.layout.round, container, false)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(::setWeek.isInitialized) setWeek.setOnClickListener(null)
        adapter?.onNextClickGuest = null
        adapter?.onNextClickOwner = null
        recyclerView?.adapter = null
        recyclerView = null
        adapter = null
        chooseWeekLayout?.removeAllViews()
        chooseWeekLayout = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.round_recyclerView)
        title = view.findViewById(R.id.round_choose_week_text)
        weekRange = view.findViewById(R.id.round_week_text)
        deadline = view.findViewById(R.id.round_deadline_text)
        chooseWeekLayout = view.findViewById(R.id.round_choose_week)
        weekLayout = view.findViewById(R.id.round_choose_week_week)
        setWeek = view.findViewById(R.id.round_choose_week_add)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        user = if(isPlayoff) playoffViewModel.user.value!!
               else scheduleViewModel.user.value!!

        if(isPlayoff) playoffViewModel.playoff.value?.let { playoff = it }

        populateFields()
        setupRecycler()
        getWeek()
        setListeners()
        setObservers()
    }

    private fun populateFields()
    {
        if(::playoff.isInitialized)
        {
            weekRange.visibility = VISIBLE
            weekRange.text = "${playoff.startDate.toMyString()} – ${playoff.endDate.toMyString()}"
            title.text = "Stav baráže"
            deadline.text = "Počet dní do konce baráže: ${DateUtil.getRemainingDaysUntil(playoff.endDate)}"
        }
        else if(user is SignedUser && (user as SignedUser).self.role.toRole() == UserRole.HEAD_OF_LEAGUE)
        {
            deadline.visibility = GONE
            weekLayout.visibility = VISIBLE
            setWeek.visibility = VISIBLE
        }
        else
        {
            group.roundDates[round.toString()]?.let { weekNumber ->
                title.text = String.format(getString(R.string.round_state), round)
                val remainingDays = DateUtil.getRemainingDaysUntil(weekNumber)
                if(remainingDays == 0) deadline.text = "Kolo již proběhlo."
                else deadline.text = String.format(getString(R.string.round_countdown), remainingDays)
            } ?: let { chooseWeekLayout?.visibility = GONE }
        }
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
                    weekRange.visibility = VISIBLE
                    weekRange.text = "${week.range.first().toMyString()} – ${week.range.last().toMyString()}"
                    weekLayout.editText?.setText(week.self.toString())
                }
            }
        }
    }

    private fun setupRecycler()
    {
        val query =
            if(isPlayoff) MatchRepository.getMatches(round, group, DateUtil.actualSeason.toInt() - 1)
            else MatchRepository.getMatches(round, group, DateUtil.actualSeason.toInt())

        val options = FirestoreRecyclerOptions.Builder<Match>()
            .setQuery(query, Match::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = ShowMatchesFirestoreAdapter(
            user,
            options,
            isPlayoff
        )

        adapter?.onNextClickOwner = { match ->
            startMatchArrangementActivity(match)
        }

        adapter?.onNextClickGuest = { match ->
            startMatchResultActivity(match, getString(R.string.match_result))
        }

        recyclerView?.adapter = adapter
    }

    private fun startMatchResultActivity(match: Match, title: String)
    {
        val intent = Intent(activity!!, MatchViewPagerActivity::class.java).apply {
            putExtra(MatchViewPagerActivity.MATCH, match)
            putExtra(MatchViewPagerActivity.WEEK, week)
            putExtra(MatchViewPagerActivity.TITLE, title)
        }
        startActivity(intent)
    }

    private fun startMatchArrangementActivity(match: Match)
    {
        val intent = Intent(activity!!, MatchArrangementActivity::class.java).apply {
            putExtra(MatchArrangementActivity.MATCH, match)
            putExtra(MatchArrangementActivity.WEEK, week)
            with(user) {
                if(this is SignedUser)
                    putExtra(MatchArrangementActivity.USER_NAME, "${self.name} ${self.surname}")
            }
        }
        startActivity(intent)
    }


}