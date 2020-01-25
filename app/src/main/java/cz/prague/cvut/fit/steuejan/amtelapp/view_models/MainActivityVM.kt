package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.states.UserState

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

    private val team = SingleLiveEvent<Team>()

    fun setTeam(team: Team)
    {
        this.team.value = team
        Log.i(TAG, "setTeam(): $team is set")
    }

    fun getTeam(): LiveData<Team> = team

    /*---------------------------------------------------*/

    companion object
    {
        const val TITLE = "title"
        const val DRAWER_POSITION = "position"
    }

    private val TAG = "MainActivityVM"
}