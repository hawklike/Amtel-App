package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.AbstractBaseFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.TeamState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.AccountTMMakeTeamFragmentVM
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class AccountTMMakeTeamFragment : AbstractBaseFragment(), CoroutineScope
{
    companion object
    {
        fun newInstance(): AccountTMMakeTeamFragment = AccountTMMakeTeamFragment()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job + handler

    private lateinit var job: Job

    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.e("CoroutineScope", "$exception handled !")
    }

    private val viewModel by viewModels<AccountTMMakeTeamFragmentVM>()

    private lateinit var team: Team
    private lateinit var user: User

    private lateinit var nameLayout: TextInputLayout
    private lateinit var placeLayout: TextInputLayout
    private lateinit var playingDaysLayout: TextInputLayout
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
        //TODO: delete previous teams if clicked more times
        createTeam.setOnClickListener {
            val name = nameLayout.editText?.text.toString().trim()
            val place = placeLayout.editText?.text.toString().trim()
            val playingDays = playingDaysLayout.editText?.text.toString().trim()

            //TODO: [REFACTORING] add deleteErrors into AbstractBaseFragment
            deleteErrors()
            viewModel.createTeam(name, place, playingDays)
        }
    }

    //TODO: observe to name, place and days
    //TODO: update text fields
    private fun setObservers()
    {
        getTeam()
        getUser()
        isTeamCreated()
    }

    private fun getTeam()
    {
        team = mainActivityModel.getTeam().value ?: Team()
        mainActivityModel.getTeam().observe(viewLifecycleOwner) { observedTeam ->
            team = observedTeam.copy()
        }
    }

    private fun getUser()
    {
        user = mainActivityModel.getUser().value ?: User()
        mainActivityModel.getUser().observe(viewLifecycleOwner) { observedUser ->
            user = observedUser.copy()
        }
    }

    //TODO: display dialog
    private fun isTeamCreated()
    {
        viewModel.isTeamCreated().observe(viewLifecycleOwner) { teamState ->
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
            mainActivityModel.setTeam(state.self)
        }
    }

    private fun deleteErrors()
    {
        nameLayout.error = null
        placeLayout.error = null
        playingDaysLayout.error = null
    }
}