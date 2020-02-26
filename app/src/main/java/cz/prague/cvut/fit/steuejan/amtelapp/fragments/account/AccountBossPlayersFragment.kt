package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowUsersFirestoreAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment

class AccountBossPlayersFragment : AbstractMainActivityFragment()
{
    companion object
    {
        fun newInstance(): AccountBossPlayersFragment = AccountBossPlayersFragment()
    }

    override fun getName(): String = "AccountBossPlayersFragment"

    private var recyclerView: RecyclerView? = null
    private var adapter: ShowUsersFirestoreAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.account_boss_players, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.account_boss_players_recyclerView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setupRecycler()
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

    private fun setupRecycler()
    {
        val query = UserManager.retrieveAllUsers()
        val options = FirestoreRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = ShowUsersFirestoreAdapter(activity!!, options)
        recyclerView?.adapter = adapter
    }

}