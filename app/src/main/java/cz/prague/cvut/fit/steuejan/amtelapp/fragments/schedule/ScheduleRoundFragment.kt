package cz.prague.cvut.fit.steuejan.amtelapp.fragments.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowMatchesFirestoreAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.InsideScheduleActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.UserState

class ScheduleRoundFragment : InsideScheduleActivityFragment()
{
    private var round = 0
    private var groupName = ""
    private lateinit var user: UserState

    private var chooseWeekLayout: RelativeLayout? = null

    private lateinit var weekLayout: TextInputLayout
    private lateinit var setWeek: FloatingActionButton

    private var recyclerView: RecyclerView? = null
    private var adapter: ShowMatchesFirestoreAdapter? = null

    companion object
    {
        private const val ROUND = "round"
        private const val NAME = "groupName"

        fun newInstance(round: Int, groupName: String): ScheduleRoundFragment
        {
            val fragment = ScheduleRoundFragment()
            fragment.arguments = Bundle().apply {
                putInt(ROUND, round)
                putString(NAME, groupName)
            }
            return fragment
        }
    }

    override fun getName(): String = "ScheduleRoundFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        arguments?.getInt(ROUND)?.let { round = it }
        arguments?.getString(NAME)?.let { groupName = it }
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
        setListeners()
        setObservers()
    }

    private fun setListeners()
    {

    }

    private fun setObservers()
    {

    }

    private fun setupRecycler()
    {
        val query = MatchManager.getMatches(round, groupName)
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