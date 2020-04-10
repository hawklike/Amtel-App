package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerInfoActivityVM : ViewModel()
{
    var mRounds: MutableList<Round>? = null
    var mGroupName: String? = null

    var mUserId = ""
    var mUser: User? = null

    /*---------------------------------------------------*/

    private val _rounds = MutableLiveData<List<Round>>()
    val rounds: LiveData<List<Round>> = _rounds

    /*---------------------------------------------------*/

    private val _player = MutableLiveData<User?>()
    val player: LiveData<User?> = _player

    /*---------------------------------------------------*/

    private val _group = MutableLiveData<String?>()
    val group: LiveData<String?> = _group

    /*---------------------------------------------------*/

    fun getRounds()
    {
        mRounds = mutableListOf()
        viewModelScope.launch {
            UserManager.retrieveRounds(mUserId)?.let { playerRounds ->
                with(playerRounds.rounds.values) {
                        forEach { rounds ->
                            mRounds?.addAll(rounds.getActiveRounds())
                        }
                    }
                withContext(Default) {
                    mRounds = mRounds?.sortedByDescending { it.date }?.toMutableList()
                }
                _rounds.value = mRounds
            }
        }
    }

    fun getPlayer()
    {
        if(mUser == null)
        {
            viewModelScope.launch {
                UserManager.findUser(mUserId)?.let {
                    mUser = it
                    mUserId = it.id!!
                    _player.value = it
                }
                ?: let { _player.value = null }
            }
        }
        else
        {
            mUserId = mUser?.id!!
            _player.value = mUser
        }
    }

    fun getGroup()
    {
        viewModelScope.launch {
            with(TeamManager.findTeam(mUser?.teamId)) {
                if(this is ValidTeam)
                {
                    mGroupName = self.groupName
                    _group.value = self.groupName
                }
                else
                {
                    mGroupName = "-"
                    _group.value = null
                }
            }
        }
    }


}
