package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.activities.AbstractBaseActivity
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.TeamState
import cz.prague.cvut.fit.steuejan.amtelapp.states.UserState
import kotlinx.coroutines.launch

class MainActivityVM(private val state: SavedStateHandle) : ViewModel()
{
    fun setTitle(title: String)
    {
        state.set(TITLE, title)
    }

    fun getTitle(): LiveData<String> = state.getLiveData(TITLE)

    /*---------------------------------------------------*/

    fun setDrawerSelectedPosition(position: Int)
    {
        state.set(DRAWER_POSITION, position)
    }

    fun getDrawerSelectedPosition(): Int = state[DRAWER_POSITION] ?: 0

    /*---------------------------------------------------*/

    private val userState = SingleLiveEvent<UserState>()

    fun setUserState(userState: UserState)
    {
        this.userState.value = userState
    }

    fun getUserState(): LiveData<UserState> = userState

    /*---------------------------------------------------*/

    private val user = SingleLiveEvent<User>()

    fun setUser(user: User)
    {
        this.user.value = user
        Log.i(TAG, "setUser(): $user is set")
    }

    fun getUser(): LiveData<User> = user

    /*---------------------------------------------------*/

    private val team = SingleLiveEvent<TeamState>()

    fun setTeam(team: TeamState)
    {
        this.team.value = team
        Log.i(TAG, "setTeam(): $team is set")
    }

    fun getTeam(): LiveData<TeamState> = team

    /*---------------------------------------------------*/

    fun prepareUser(uid: String)
    {
        viewModelScope.launch {
            val user = UserManager.findUser(uid)
            user?.let {
                setUserState(SignedUser(it))
                setUser(it)
                Log.i(AbstractBaseActivity.TAG, "displayAccount(): $user currently logged in")
            }
        }
    }

    companion object
    {
        const val TITLE = "title"
        const val DRAWER_POSITION = "position"
    }

    private val TAG = "MainActivityVM"
}