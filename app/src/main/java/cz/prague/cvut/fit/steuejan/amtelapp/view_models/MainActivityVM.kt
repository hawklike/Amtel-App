package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.*
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.AbstractBaseActivity
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.EmailManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.EmailSender
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
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

    fun getDrawerSelectedPosition(): LiveData<Int> = state.getLiveData(DRAWER_POSITION)

    /*---------------------------------------------------*/

    private val userState = SingleLiveEvent<UserState>()

    fun isUserLoggedIn(userState: UserState)
    {
        this.userState.value = userState
    }

    fun isUserLoggedIn(): LiveData<UserState> = userState

    /*---------------------------------------------------*/

    private val user = SingleLiveEvent<User?>()

    fun setUser(user: User?)
    {
        this.user.value = user
        Log.i(TAG, "setUser(): $user is set")
    }

    fun getUser(): LiveData<User?> = user

    /*---------------------------------------------------*/

    private val team = SingleLiveEvent<TeamState>()

    fun setTeam(team: TeamState)
    {
        this.team.value = team
        Log.i(TAG, "setTeam(): $team is set")
    }

    fun getTeam(): LiveData<TeamState> = team

    /*---------------------------------------------------*/

    private val _progressBar = MutableLiveData<Boolean>()
    val progressBar: LiveData<Boolean> = _progressBar
    fun setProgressBar(on: Boolean)
    {
        _progressBar.value = on
    }

    /*---------------------------------------------------*/

    fun prepareUser(uid: String)
    {
        viewModelScope.launch {
            val user = UserManager.findUser(uid)
            user?.let {
                isUserLoggedIn(SignedUser(it))
                setUser(it)
                Log.i(AbstractBaseActivity.TAG, "displayAccount(): $user currently logged in")
            }
        }
    }

    fun initEmailPassword()
    {
        PreferenceManager
            .getDefaultSharedPreferences(context)
            ?.getString(context.getString(R.string.email_password_key), null)?.let {
                EmailSender.hasPassword = true
            } ?: let {
                viewModelScope.launch {
                    EmailManager.getPassword()?.let {
                        EmailSender.hasPassword = true
                        PreferenceManager.
                            getDefaultSharedPreferences(context)
                            .edit()
                            .putString(context.getString(R.string.email_password_key), it)
                            .apply()
                    }
                }
            }
    }

    fun initHeadOfLeagueEmail()
    {
        if(EmailSender.headOfLeagueEmail == null)
        {
            viewModelScope.launch {
                val headOfLeague = UserManager.findUsers("role", UserRole.HEAD_OF_LEAGUE.toString())?.first()
                headOfLeague?.let {
                    EmailSender.headOfLeagueEmail = it.email
                }
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