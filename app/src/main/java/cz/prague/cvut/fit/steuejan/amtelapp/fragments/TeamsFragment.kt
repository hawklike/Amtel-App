package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.TeamInfoActivity
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.paging.ShowTeamsPagingAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.TeamOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.FragmentTeamsBinding
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment

class TeamsFragment : AbstractMainActivityFragment()
{
    private var _binding: FragmentTeamsBinding? = null
    private val binding get() = _binding!!

    private var adapter: ShowTeamsPagingAdapter? = null

    companion object
    {
        fun newInstance(): TeamsFragment = TeamsFragment()
    }

    override fun getName(): String = "TeamsFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = FragmentTeamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView()
    {
        super.onDestroyView()

        adapter?.onClick = null
        binding.teams.adapter = null
        adapter = null

        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle(getString(R.string.teams))
        setLogoutIconVisibility(false)
        showTeams()
        sortTeams()
    }

    private fun showTeams()
    {
        binding.teams.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }

        val options =  setQueryOptions(TeamOrderBy.NAME)

        adapter = ShowTeamsPagingAdapter(options)
        adapter?.onClick = { team ->
            team?.let {
                val intent = Intent(activity, TeamInfoActivity::class.java).apply {
                    putExtra(TeamInfoActivity.TEAM, it)
                }
                startActivity(intent)
            }
        }

        binding.teams.adapter = adapter
    }

    private fun sortTeams()
    {
        binding.sortBy.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId)
            {
                binding.sortByName.id -> adapter?.updateOptions(setQueryOptions(TeamOrderBy.NAME))
                binding.sortByGroup.id -> adapter?.updateOptions(setQueryOptions(TeamOrderBy.GROUP))
            }
        }
    }

    private fun setQueryOptions(orderBy: TeamOrderBy): FirestorePagingOptions<Team>
    {
        val query = TeamManager.retrieveAllTeams(orderBy)
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(2)
            .setPageSize(5)
            .build()

        return FirestorePagingOptions.Builder<Team>()
            .setQuery(query, config, Team::class.java)
            .setLifecycleOwner(this)
            .build()
    }


}