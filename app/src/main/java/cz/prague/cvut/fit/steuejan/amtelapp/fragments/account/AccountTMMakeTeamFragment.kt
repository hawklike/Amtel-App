package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.AddUserToTeamActivity
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.AbstractBaseFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.AccountTMMakeTeamFragmentVM
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AccountTMMakeTeamFragment : AbstractBaseFragment()
{
    companion object
    {
        fun newInstance(): AccountTMMakeTeamFragment = AccountTMMakeTeamFragment()
    }

    override lateinit var job: Job

    private val viewModel by viewModels<AccountTMMakeTeamFragmentVM>()

    private lateinit var team: TeamState
    private lateinit var user: User

    private lateinit var nameLayout: TextInputLayout
    private lateinit var placeLayout: TextInputLayout
    private lateinit var playingDaysLayout: TextInputLayout
    private lateinit var addPlayer: ImageButton

    private lateinit var createTeam: FloatingActionButton

    override fun getName(): String = "AccountTMMakeTeamFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        job = Job()
        return inflater.inflate(R.layout.account_tm_make_team, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        nameLayout = view.findViewById(R.id.account_tm_make_team_name)
        placeLayout = view.findViewById(R.id.account_tm_make_team_place)
        playingDaysLayout = view.findViewById(R.id.account_tm_make_team_playing_day)
        createTeam = view.findViewById(R.id.account_tm_make_team_create)
        addPlayer = view.findViewById(R.id.account_tm_make_team_add_player_button)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setObservers()
        setListeners()
    }

    override fun onDestroy()
    {
        job.cancel()
        super.onDestroy()
    }

    private fun setListeners()
    {
        playingDaysLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus)
            {
                MaterialDialog(activity!!).show {
                    listItemsMultiChoice(R.array.days) { _, _, items ->
                        playingDaysLayout.editText?.setText(items.joinToString(", "))
                    }
                    positiveButton(R.string.ok)
                }
            }
        }

        createTeam.setOnClickListener {
            val name = nameLayout.editText?.text.toString().trim()
            val place = placeLayout.editText?.text.toString().trim()
            val playingDays = playingDaysLayout.editText?.text.toString().trim()


            //TODO: [REFACTORING] add deleteErrors into AbstractBaseFragment
            deleteErrors()
            viewModel.createTeam(
                user,
                name,
                place,
                playingDays)
        }

        addPlayer.setOnClickListener {
            if(::team.isInitialized && team is ValidTeam)
            {
                val intent = Intent(activity!!, AddUserToTeamActivity::class.java).apply {
                    putExtra(AddUserToTeamActivity.TEAM, (team as ValidTeam).self)
                }
                startActivity(intent)
            }
            else(Log.e(TAG, "Failed to start AddUserToTeamActivity because team is not valid or initialized yet."))
        }

    }

    private fun setObservers()
    {
        getTeam()
        getUser()
        updateFields()
        confirmName()
        confirmPlace()
        confirmDays()
        isTeamCreated()
    }

    private fun updateFields()
    {
        if(team is ValidTeam)
        {
            nameLayout.editText?.setText((team as ValidTeam).self.name)
            placeLayout.editText?.setText((team as ValidTeam).self.place)
            playingDaysLayout.editText?.setText((team as ValidTeam).self.playingDays.joinToString(", "))
        }
    }

    private fun confirmDays()
    {
        viewModel.confirmPlayingDays().observe(viewLifecycleOwner) { daysState ->
            if(daysState is InvalidPlayingDays)
                playingDaysLayout.error = daysState.errorMessage
        }
    }

    private fun confirmPlace()
    {
        viewModel.confirmPlace().observe(viewLifecycleOwner) { placeState ->
            if(placeState is InvalidPlace)
                placeLayout.error = placeState.errorMessage
        }
    }

    private fun confirmName()
    {
        viewModel.confirmName().observe(viewLifecycleOwner) { nameState ->
            if(nameState is InvalidName)
                nameLayout.error = nameState.errorMessage
        }
    }

    private fun getTeam()
    {
        team = mainActivityModel.getTeam().value ?: ValidTeam(Team())
        mainActivityModel.getTeam().observe(viewLifecycleOwner) { observedTeam ->
            team = observedTeam
        }
    }

    private fun getUser()
    {
        user = mainActivityModel.getUser().value ?: User()
        mainActivityModel.getUser().observe(viewLifecycleOwner) { observedUser ->
            user = observedUser.copy()
        }
    }

    private fun isTeamCreated()
    {
        viewModel.isTeamCreated().observe(viewLifecycleOwner) { teamState ->
            val title = viewModel.displayAfterDialog(teamState, user).title

            MaterialDialog(activity!!)
                .title(text = title)
                .show {
                    positiveButton(R.string.ok)
                    onDismiss {
                    }
                }

            update(teamState)
        }
    }

    private fun update(state: TeamState)
    {
        if(state is ValidTeam)
        {
            user.teamId = state.self.id
            launch {
                UserManager.updateUser(user.id, mapOf("teamId" to user.teamId))
            }
            mainActivityModel.setUser(user)
            mainActivityModel.setTeam(ValidTeam(state.self))
        }
    }

    private fun deleteErrors()
    {
        nameLayout.error = null
        placeLayout.error = null
        playingDaysLayout.error = null
    }
}