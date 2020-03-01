package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidSet
import cz.prague.cvut.fit.steuejan.amtelapp.states.SetState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidSet
import kotlinx.coroutines.launch

@Suppress("PrivatePropertyName")
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

    private val _isInputOk = SingleLiveEvent<Boolean>()
    val isInputOk: LiveData<Boolean> = _isInputOk

    /*---------------------------------------------------*/

    private var m_isInputOk = true
    var round: Int = 1
    lateinit var match: Match

    /*---------------------------------------------------*/

    //TODO: send an email after the result is input
    //TODO: let a user to input the result twice (head of league unlimited)
    //TODO: if a team manager is away's team manager, display only info overview
    //TODO: retrieve updated match

    /**
     * Call this method before inputResult() method
     */
    fun confirmInput(firstHome: String, firstAway: String, secondHome: String, secondAway: String, thirdHome: String, thirdAway: String, homePlayersText: Editable, awayPlayersText: Editable, isFiftyGroup: Boolean)
    {
        viewModelScope.launch {
            m_isInputOk = true

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

            _isInputOk.value = m_isInputOk
        }
    }

    /**
     * Call this method only if confirmInput() returns true
     */
    fun inputResult()
    {
        viewModelScope.launch {
            var match = this@MatchInputResultFragmentVM.match

            val home1: Int = (firstHome.value as ValidSet).self
            val home2: Int = (secondHome.value as ValidSet).self
            val home3: Int? = with((thirdHome.value as ValidSet).self) {
                if(this == Int.MAX_VALUE) null
                else this
            }

            val away1: Int = (firstAway.value as ValidSet).self
            val away2: Int = (secondAway.value as ValidSet).self
            val away3: Int? = with((thirdAway.value as ValidSet).self) {
                if(this == Int.MAX_VALUE) null
                else this
            }

            match = calculateScore(match, home1, away1, home2, away2, home3, away3)
            MatchManager.addMatch(match)
            toast("OK")
        }
    }

    private fun calculateScore(match: Match, home1: Int, away1: Int, home2: Int, away2: Int, home3: Int?, away3: Int?): Match
    {
        val homeGames = home1 + home2 + (home3 ?: 0)
        val awayGames = away1 + away2 + (away3 ?: 0)

        var homeSets = 0
        var awaySets = 0

        if(home1 > away1) homeSets++
        else awaySets++

        if(home2 > away2) homeSets++
        else awaySets++

        if(home3 != null && away3 != null)
        {
            if(home3 > away3) homeSets++
            else awaySets++
        }

        match.rounds[round - 1] = Round(homeSets, awaySets, homeGames, awayGames, home1, away1, home2, away2, home3, away3)
        return match
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
                m_isInputOk = false
                home.value = InvalidSet(context.getString(R.string.games_invalid_rules_error))
                away.value = InvalidSet(context.getString(R.string.games_invalid_rules_error))
            }
        }
    }

    private fun confirmGames(games: String, data: MutableLiveData<SetState>, optional: Boolean = false)
    {
        with(SetState.validate(games, optional)) {
            if(this is InvalidSet) m_isInputOk = false
            data.value = this
        }
    }

    private fun confirmPlayers(players: Editable, data: MutableLiveData<Boolean>)
    {
        if(players.isEmpty())
        {
            data.value = false
            m_isInputOk = false
        }
        else
        {
            if(players.split(",").size > 2)
            {
                data.value = false
                m_isInputOk = false
            }
        }
    }
}
