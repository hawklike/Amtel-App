package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidSet
import cz.prague.cvut.fit.steuejan.amtelapp.states.SetState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidSet
import kotlinx.coroutines.launch

class MatchInputResultFragmentVM : ViewModel()
{
    private val _firstHome = MutableLiveData<SetState>()
    val firstHome: LiveData<SetState> = _firstHome

    /*---------------------------------------------------*/

    private val _firstAway = MutableLiveData<SetState>()
    val firstAway: LiveData<SetState> = _firstAway

    /*---------------------------------------------------*/

    private val _secondHome = MutableLiveData<SetState>()
    val secondHome: LiveData<SetState> = _secondHome

    /*---------------------------------------------------*/

    private val _secondAway = MutableLiveData<SetState>()
    val secondAway: LiveData<SetState> = _secondAway

    /*---------------------------------------------------*/

    private val _thirdHome = MutableLiveData<SetState>()
    val thirdHome: LiveData<SetState> = _thirdHome

    /*---------------------------------------------------*/

    private val _thirdAway = MutableLiveData<SetState>()
    val thirdAway: LiveData<SetState> = _thirdAway

    /*---------------------------------------------------*/

    private val _homePlayers = MutableLiveData<Boolean>()
    val homePlayers: LiveData<Boolean> = _homePlayers

    /*---------------------------------------------------*/

    private val _awayPlayers = MutableLiveData<Boolean>()
    val awayPlayers: LiveData<Boolean> = _awayPlayers

    /*---------------------------------------------------*/

    private var isInputOk = true
    var round: Int = 0

    /*---------------------------------------------------*/

    fun inputResult(firstHome: String, firstAway: String, secondHome: String, secondAway: String, thirdHome: String, thirdAway: String, homePlayersText: Editable, awayPlayersText: Editable, isFiftyGroup: Boolean)
    {
        viewModelScope.launch {
            if(confirmInput(firstHome, firstAway, secondHome, secondAway, thirdHome, thirdAway, homePlayersText, awayPlayersText, isFiftyGroup))
            {
                val homeGems = firstHome.toInt() + secondHome.toInt() + if(thirdHome.isNotEmpty()) thirdHome.toInt() else 0
                toast("OK")
            }

        }
    }

    private fun confirmInput(firstHome: String, firstAway: String, secondHome: String, secondAway: String, thirdHome: String, thirdAway: String, homePlayersText: Editable, awayPlayersText: Editable, isFiftyGroup: Boolean): Boolean
    {
        isInputOk = true
        confirmGames(firstHome, _firstHome)
        confirmGames(firstAway, _firstAway)
        confirmGames(secondHome, _secondHome)
        confirmGames(secondAway, _secondAway)
        confirmGames(thirdHome, _thirdHome, optional = true)
        confirmGames(thirdAway, _thirdAway, optional = true)

        confirmSet(_firstHome, _firstAway)
        confirmSet(_secondHome, _secondAway)
        if(!isFiftyGroup) confirmSet(_thirdHome, _thirdAway)

        confirmPlayers(homePlayersText, _homePlayers)
        confirmPlayers(awayPlayersText, _awayPlayers)

        return isInputOk
    }

    private fun confirmSet(home: MutableLiveData<SetState>, away: MutableLiveData<SetState>)
    {
        if(home.value is ValidSet && away.value is ValidSet)
        {
            val homeGames = (home.value as ValidSet).self
            val awayGames = (away.value as ValidSet).self

            if(homeGames == Int.MAX_VALUE && awayGames == Int.MAX_VALUE) return

            if(!SetState.validate(homeGames, awayGames))
            {
                isInputOk = false
                home.value = InvalidSet(context.getString(R.string.games_invalid_rules_error))
                away.value = InvalidSet(context.getString(R.string.games_invalid_rules_error))
            }
        }
    }

    private fun confirmGames(games: String, data: MutableLiveData<SetState>, optional: Boolean = false)
    {
        with(SetState.validate(games, optional)) {
            if(this is InvalidSet) isInputOk = false
            data.value = this
        }
    }

    private fun confirmPlayers(players: Editable, data: MutableLiveData<Boolean>)
    {
        if(players.isEmpty())
        {
            data.value = false
            isInputOk = false
        }
        else
        {
            if(players.split(",").size > 2)
            {
                data.value = false
                isInputOk = false
            }
        }
    }
}
