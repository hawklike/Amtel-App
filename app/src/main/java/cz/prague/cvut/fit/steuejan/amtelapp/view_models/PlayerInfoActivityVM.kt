package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Player
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerInfoActivityVM : ViewModel()
{
    var mRounds: MutableList<Round>? = null

    /*---------------------------------------------------*/

    private val _rounds = MutableLiveData<List<Round>>()
    val rounds: LiveData<List<Round>> = _rounds

    /*---------------------------------------------------*/

    fun getRounds(player: Player)
    {
        mRounds = mutableListOf()
        viewModelScope.launch {
            UserManager.retrieveRounds(player.playerId)?.let { playerRounds ->
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

}
