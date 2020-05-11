package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.AddUserToTeamActivity
import cz.prague.cvut.fit.steuejan.amtelapp.activities.AddUserToTeamActivity.Companion.TEAM
import cz.prague.cvut.fit.steuejan.amtelapp.activities.EditUserActivity
import cz.prague.cvut.fit.steuejan.amtelapp.activities.PlayerInfoActivity
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal.ShowTeamPlayersAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole.TEAM_MANAGER
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments.AccountTMMakeTeamFragmentVM

class AccountTMMakeTeamFragment : AbstractMainActivityFragment()
{
    companion object
    {
        fun newInstance(): AccountTMMakeTeamFragment = AccountTMMakeTeamFragment()
        const val NEW_USER_CODE = 1
        const val EDIT_USER_CODE = 2
    }

    private val viewModel by viewModels<AccountTMMakeTeamFragmentVM>()

    private lateinit var team: TeamState
    private lateinit var user: User

    private var users = mutableListOf<User>()

    private var createTeamLayout: RelativeLayout? = null

    private lateinit var nameLayout: TextInputLayout
    private lateinit var placeLayout: TextInputLayout
    private lateinit var playingDaysLayout: TextInputLayout
    private lateinit var createTeam: FloatingActionButton

    private lateinit var addPlayer: RelativeLayout
    private lateinit var refreshLayout: SwipeRefreshLayout

    private var recyclerView: RecyclerView? = null
    private var adapter: ShowTeamPlayersAdapter? = null

    override fun getName(): String = "AccountTMMakeTeamFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.account_tm_make_team, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        createTeamLayout = view.findViewById(R.id.account_tm_make_team)
        nameLayout = view.findViewById(R.id.account_tm_make_team_name)
        placeLayout = view.findViewById(R.id.account_tm_make_team_place)
        playingDaysLayout = view.findViewById(R.id.account_tm_make_team_playing_day)
        createTeam = view.findViewById(R.id.account_tm_make_team_create)
        addPlayer = view.findViewById(R.id.account_tm_make_team_add_player)
        recyclerView = view.findViewById(R.id.account_tm_make_team_players_recyclerView)
        refreshLayout = view.findViewById(R.id.account_tm_make_team_refresh_players)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        getUser()
        getTeam()
        setupRecycler()
        setupRefreshLayout()
        updateFields()
        setObservers()
        setListeners()
    }

    override fun onResume()
    {
        super.onResume()
        isLineUpAllowed()
        if(::team.isInitialized && team is ValidTeam)
        {
            val users = (team as ValidTeam).self.users
            populateAdapter(users)
        }
        else refreshLayout.isRefreshing = false
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        adapter?.onDelete = null
        adapter?.onClick = null
        adapter?.onLongClick = null
        adapter?.onEdit = null
        recyclerView?.adapter = null
        recyclerView = null
        adapter = null

        createTeam.setOnClickListener(null)
        addPlayer.setOnClickListener(null)
        playingDaysLayout.editText?.setOnClickListener(null)

        createTeamLayout?.removeAllViews()
        createTeamLayout = null
    }

    private fun isLineUpAllowed()
    {
        viewModel.isLineUpAllowed()
        viewModel.isLineUpAllowed.observe(viewLifecycleOwner) { allowed ->
            if(!mainActivityModel.isDeadlineAlertShown)
            {
                MaterialDialog(activity!!).show {
                    title(text = getString(R.string.line_up_closing))
                    message(text = viewModel.deadlineDialog)
                    positiveButton()
                }
                mainActivityModel.isDeadlineAlertShown = true
            }

            adapter?.isAllowed = allowed
            adapter?.notifyDataSetChanged()
            if(allowed)
            {
                view?.findViewById<TextView>(R.id.account_tm_make_team_add_player_text)?.alpha = 1f
                view?.findViewById<ImageView>(R.id.account_tm_make_team_add_player_button)?.alpha = 1f
                addPlayer.setOnClickListener { addPlayer() }
            }
        }
    }

    private fun setupRecycler()
    {
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = ShowTeamPlayersAdapter(activity!!, users)

        adapter?.onDelete = {
            if(team is ValidTeam)
            {
                val tmp = (team as ValidTeam).self
                tmp.users.clear()
                tmp.users.addAll(it)
                mainActivityModel.setTeam(team)
            }
        }

        adapter?.onClick = { user ->
            val intent = Intent(activity, PlayerInfoActivity::class.java).apply {
                putExtra(PlayerInfoActivity.PLAYER, user)
            }
            startActivity(intent)
        }

        adapter?.onEdit = { user ->
            if(user.role.toRole() == TEAM_MANAGER)
            {
                mainActivityModel.setAccountPage(AccountFragment.PERSONAL_TM)
                toast(R.string.you_may_change_your_personal_information_here)
            }
            else
            {
                val intent = Intent(activity, EditUserActivity::class.java).apply {
                    putExtra(EditUserActivity.USER, user)
                }
                startActivityForResult(intent, EDIT_USER_CODE)
            }
        }

        recyclerView?.adapter = adapter
    }

    private fun setupRefreshLayout()
    {
        refreshLayout.setColorSchemeResources(R.color.blue)
        refreshLayout.setOnRefreshListener {
            onResume()
        }
    }

    private fun setListeners()
    {
        playingDaysLayout.editText?.setOnClickListener {
            MaterialDialog(activity!!).show {
                title(R.string.choose_playing_days)

                val indices = playingDaysLayout.editText?.text?.let {
                    viewModel.setDialogDays(it)
                } ?: intArrayOf()

                listItemsMultiChoice(R.array.days, initialSelection = indices) { _, _, items ->
                    val sortedDays = viewModel.getDialogDays(items)
                    playingDaysLayout.editText?.setText(sortedDays.joinToString(", "))
                }
                positiveButton(R.string.ok)
            }
        }

        createTeam.setOnClickListener {
            val name = nameLayout.editText?.text.toString().trim()
            val place = placeLayout.editText?.text.toString().trim()
            val playingDays = playingDaysLayout.editText?.text.toString().trim()

            deleteErrors()

            val message = """
${if(team is NoTeam) "Název týmu: $name\nAdresa kurtu: $place" else "Adresa kurtu: $place"}
Hrací dny: $playingDays${if(team is NoTeam) "\n\nNázev týmu již nepůjde vícekrát změnit!" else ""}
            """.trimIndent()

            MaterialDialog(activity!!)
                .title(text = if(team is NoTeam) "Uložit tým?" else "Aktualizovat tým?")
                .message(text = message)
                .show {
                    positiveButton(text = if(team is NoTeam) "Uložit" else "Aktualizovat") {
                        progressDialog.show()
                        viewModel.createTeam(user, name, place, playingDays)
                    }
                    negativeButton()
                }
        }

        addPlayer.setOnClickListener { toast(R.string.line_up_closed_cant_add_player) }
    }

    private fun addPlayer()
    {
        if(team is ValidTeam)
        {
            val intent = Intent(activity!!, AddUserToTeamActivity::class.java).apply {
                putExtra(TEAM, (team as ValidTeam).self)
            }
            startActivityForResult(intent, NEW_USER_CODE)
        }
        else
        {
            toast(getString(R.string.no_team_alert))
            Log.e(TAG, "Failed to start AddUserToTeamActivity because team is not valid or initialized yet.")
        }
    }

    private fun setObservers()
    {
        confirmName()
        confirmPlace()
        confirmDays()
        isTeamCreated()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == NEW_USER_CODE && resultCode == RESULT_OK)
        {
            val team = data?.getParcelableExtra<Team>(AddUserToTeamActivity.TEAM)
            team?.let {
                mainActivityModel.setTeam(ValidTeam(it))
            }
        }

       if(requestCode == EDIT_USER_CODE && resultCode == RESULT_OK)
       {
           viewModel.getUpdatedTeam(team)
           viewModel.team.observe(viewLifecycleOwner) {
               refreshLayout.isRefreshing = true
               mainActivityModel.setTeam(ValidTeam(it))
               populateAdapter(it.users)
           }
       }
    }

    private fun updateFields()
    {
        if(team is ValidTeam)
        {
            nameLayout.editText?.setText((team as ValidTeam).self.name)
            placeLayout.editText?.setText((team as ValidTeam).self.place)
            playingDaysLayout.editText?.setText((team as ValidTeam).self.
                playingDays.
                joinToString(", ") {
                    it.trim()
                })
            disableName()
        }
        else
        {
            view!!.findViewById<ImageView>(R.id.account_tm_make_team_add_player_button).visibility = View.GONE
        }
    }

    private fun populateAdapter(users: List<User>)
    {
        this.users.clear()
        this.users.addAll(users.sortedBy { it.role })
        adapter?.notifyDataSetChanged()
        refreshLayout.isRefreshing = false
    }

    private fun confirmDays()
    {
        viewModel.playingDays.observe(viewLifecycleOwner) { daysState ->
            if(daysState is InvalidPlayingDays)
            {
                progressDialog.dismiss()
                playingDaysLayout.error = daysState.errorMessage
            }
        }
    }

    private fun confirmPlace()
    {
        viewModel.place.observe(viewLifecycleOwner) { placeState ->
            if(placeState is InvalidPlace)
            {
                progressDialog.dismiss()
                placeLayout.error = placeState.errorMessage
            }
        }
    }

    private fun confirmName()
    {
        viewModel.name.observe(viewLifecycleOwner) { nameState ->
            if(nameState is InvalidName)
            {
                progressDialog.dismiss()
                nameLayout.error = nameState.errorMessage
            }
        }
    }

    private fun getTeam()
    {
        team = mainActivityModel.getTeam().value ?: NoTeam
        mainActivityModel.getTeam().observe(viewLifecycleOwner) { observedTeam ->
            team = observedTeam
        }
    }

    private fun getUser()
    {
        user = mainActivityModel.getUser().value ?: User()
    }

    private fun isTeamCreated()
    {
        viewModel.newTeam.observe(viewLifecycleOwner) { teamState ->
            progressDialog.dismiss()
            val title = viewModel.displayAfterDialog(teamState, user).title

            MaterialDialog(activity!!)
                .title(text = title)
                .show {
                    positiveButton(R.string.ok)
                    onDismiss {}
                }

            update(teamState)
        }
    }

    private fun update(team: TeamState)
    {
        if(team is ValidTeam)
        {
            if(users.isEmpty())
            {
                users.addAll(team.self.users)
                try { adapter?.notifyItemInserted(0) }
                catch(ex: Exception) { adapter?.notifyDataSetChanged() }
            }
            user.teamId = team.self.id
            user.teamName = team.self.name
            viewModel.updateUser(user, team.self)
            mainActivityModel.setUser(user)
            mainActivityModel.setTeam(ValidTeam(team.self))
            disableName()
            view!!.findViewById<ImageView>(R.id.account_tm_make_team_add_player_button).visibility = View.VISIBLE
        }
    }

    private fun disableName()
    {
        nameLayout.editText?.text?.let {
            if(it.isNotEmpty())
            {
                nameLayout.editText?.isEnabled = false
                nameLayout.editText?.setTextColor(ContextCompat.getColor(activity!!, R.color.lightGrey))
            }
        }
    }

    private fun deleteErrors()
    {
        nameLayout.error = null
        placeLayout.error = null
        playingDaysLayout.error = null
    }
}