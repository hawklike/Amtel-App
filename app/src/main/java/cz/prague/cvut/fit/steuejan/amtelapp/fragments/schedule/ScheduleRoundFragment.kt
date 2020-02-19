package cz.prague.cvut.fit.steuejan.amtelapp.fragments.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowMatchesFirestoreAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.AbstractBaseFragment

class ScheduleRoundFragment : AbstractBaseFragment()
{
    private var round = 0
    private var groupName = ""

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

    override fun setProgressBar(on: Boolean) {}

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
    }

    override fun onDestroy()
    {
        super.onDestroy()
        adapter = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.schedule_round_recyclerView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setupRecycler()
    }

    private fun setupRecycler()
    {
        val query = MatchManager.getMatches(round, groupName)
        val options = FirestoreRecyclerOptions.Builder<Match>()
            .setQuery(query, Match::class.java)
            .build()

        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = ShowMatchesFirestoreAdapter(activity!!, options)
        recyclerView?.adapter = adapter
    }
}