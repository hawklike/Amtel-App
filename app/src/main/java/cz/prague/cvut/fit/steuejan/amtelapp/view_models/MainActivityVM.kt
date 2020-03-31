package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.AbstractBaseActivity
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.EmailManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.LeagueManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.EmailSender
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.TeamState
import cz.prague.cvut.fit.steuejan.amtelapp.states.UserState
import kotlinx.coroutines.delay
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

    private val _progressBar = SingleLiveEvent<Boolean>()
    val progressBar: LiveData<Boolean> = _progressBar
    fun setProgressBar(on: Boolean)
    {
        _progressBar.value = on
    }

    /*---------------------------------------------------*/

    private val _connection = SingleLiveEvent<Boolean>()
    val connection: LiveData<Boolean> = _connection

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
        val email = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.email_password_key), null)

        if(email == null)
        {
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
        else EmailSender.hasPassword = true
    }

    fun getActualSeason()
    {
        if(DateUtil.actualSeason.toInt() == 0)
        {
            viewModelScope.launch {
                LeagueManager.getActualSeason()?.let {
                    DateUtil.actualSeason = it.toString()
                    _connection.value = true
                }
                ?: let { _connection.value = false }
            }
        }
        else _connection.value = true
    }

    fun initHeadOfLeagueEmail(tries: Int = 10)
    {
        viewModelScope.launch {
            if(EmailSender.headOfLeagueEmail == null)
            {
                UserManager.findUsers("role", UserRole.HEAD_OF_LEAGUE.toString())?.let {
                    if(it.isNotEmpty())
                    {
                        val headOfLeague = it.first()
                        EmailSender.headOfLeagueEmail = headOfLeague.email
                    }
                    else if(tries != 0)
                    {
                        delay(5000)
                        initHeadOfLeagueEmail(tries - 1)
                    }
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