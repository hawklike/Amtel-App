package cz.prague.cvut.fit.steuejan.amtelapp.fragments.miscellaneous

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.Query
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.EditUserActivity
import cz.prague.cvut.fit.steuejan.amtelapp.activities.PlayerInfoActivity
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.paging.ShowUsersPagingAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.UserRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.FragmentPlayersBinding
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments.PlayersFragmentVM

class PlayersFragment : AbstractMainActivityFragment(), ShowUsersPagingAdapter.DataLoadedListener
{
    private var _binding: FragmentPlayersBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<PlayersFragmentVM>()

    private var adapter: ShowUsersPagingAdapter? = null

    companion object
    {
        fun newInstance(): PlayersFragment = PlayersFragment()

        const val EDIT_USER_REQUEST = 1
    }

    override fun getName(): String = "PlayersFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = FragmentPlayersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView()
    {
        super.onDestroyView()

        adapter?.onDelete = null
        adapter?.onEdit = null
        adapter?.onClick = null
        adapter?.dataLoadedListener = null
        binding.users.adapter = null
        adapter = null

        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle(getString(R.string.players))
        setLogoutIconVisibility(false)
        refreshUsers()
        showUsers()
        sortUsers()
        searchUser()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == EDIT_USER_REQUEST && resultCode == RESULT_OK) adapter?.refresh()
    }

    private fun refreshUsers()
    {
        binding.refreshLayout.setColorSchemeResources(R.color.blue)
        binding.refreshLayout.setOnRefreshListener {
            adapter?.refresh()
        }
    }

    private fun showUsers()
    {
        binding.users.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }

        val options =  setQueryOptions(viewModel.orderBy)

        val currentUser = mainActivityModel.getUser().value
        adapter = ShowUsersPagingAdapter(options, currentUser)
        adapter?.dataLoadedListener = this

        adapter?.onDelete = { user ->
            deleteUser(user)
        }

        adapter?.onEdit = { user ->
            val intent = Intent(activity, EditUserActivity::class.java).apply {
                putExtra(EditUserActivity.USER, user)
            }
            startActivityForResult(intent, EDIT_USER_REQUEST)
        }

        adapter?.onClick = { user ->
            val intent = Intent(activity, PlayerInfoActivity::class.java).apply {
                putExtra(PlayerInfoActivity.PLAYER, user)
            }
            startActivity(intent)
        }

        binding.users.adapter = adapter
    }

    private fun deleteUser(user: User?)
    {
        MaterialDialog(activity!!)
            .title(R.string.delete_user_confirmation_message)
            .show {
                positiveButton(text = "Smazat") {
                    binding.refreshLayout.isRefreshing = true
                    viewModel.deleteUser(user)
                }
                negativeButton()
            }

        viewModel.isUserDeleted.observe(viewLifecycleOwner) { deleted ->
            if(deleted) toast("Hráč byl úspěšně smazán.")
            else toast("Hráče se nepodařilo smazat.")
            adapter?.refresh()
        }
    }

    private fun sortUsers()
    {
        binding.sortBy.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId)
            {
                binding.sortByName.id -> {
                    adapter?.updateOptions(setQueryOptions(UserOrderBy.NAME))
                }

                binding.sortBySurname.id -> {
                    adapter?.updateOptions(setQueryOptions(UserOrderBy.SURNAME))
                }

                binding.sortByTeam.id -> {
                    adapter?.updateOptions(setQueryOptions(UserOrderBy.TEAM))
                }

                binding.sortByRole.id -> {
                    adapter?.updateOptions(setQueryOptions(UserOrderBy.ROLE))
                }

                binding.sortByAge.id -> {
                    adapter?.updateOptions(setQueryOptions(UserOrderBy.AGE))
                }
            }
        }
    }

    private fun searchUser()
    {
        binding.search.addTextChangedListener(object: TextWatcher
        {
            override fun afterTextChanged(text: Editable)
            {
                if(text.isNotEmpty())
                {
                    val query = UserRepository.retrieveUsersByPrefix(text.toString())
                    viewModel.query = query.orderBy(UserRepository.searchSurname)
                    adapter?.updateOptions(setQueryOptions(viewModel.orderBy))
                }
                else
                {
                    viewModel.query = UserRepository.retrieveAllUsers()
                    adapter?.updateOptions(setQueryOptions(viewModel.orderBy))
                }
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setQueryOptions(orderBy: UserOrderBy): FirestorePagingOptions<User>
    {
        viewModel.orderBy = orderBy
        adapter?.orderBy = orderBy
        val query = viewModel.query.orderBy(orderBy.toString())
        return setQueryOptions(query)
    }

    private fun setQueryOptions(query: Query): FirestorePagingOptions<User>
    {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(6)
            .setPageSize(5)
            .build()

        return FirestorePagingOptions.Builder<User>()
            .setQuery(query, config, User::class.java)
            .setLifecycleOwner(this)
            .build()
    }

    override fun onLoaded()
    {
        binding.refreshLayout.isRefreshing = false
    }

    override fun onLoading()
    {
        binding.refreshLayout.isRefreshing = true
    }
}