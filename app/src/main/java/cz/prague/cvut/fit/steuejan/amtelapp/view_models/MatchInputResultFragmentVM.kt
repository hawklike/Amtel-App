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
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.removeWhitespaces
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Player
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
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

    private val _matchAdded = MutableLiveData<Match>()
    val matchAdded: LiveData<Match> = _matchAdded

    /*---------------------------------------------------*/

    private val _isTie = SingleLiveEvent<Boolean>()
    val isTie: LiveData<Boolean> = _isTie

    /*---------------------------------------------------*/

    private var m_isInputOk = true
    var round: Int = 1

    private lateinit var homePlayersText: String
    private lateinit var awayPlayersText: String

    /*---------------------------------------------------*/

    private lateinit var oldRound: Round
    private lateinit var match: Match
    fun setMatch(match: Match)
    {
        this.match = match
        oldRound = match.rounds[round - 1].copy()
    }

    /*---------------------------------------------------*/


    //TODO: send an email after the result is input
    //TODO: let a user to input the result twice (head of league unlimited) (DONE - need to be tested)
    //TODO: if a team manager is away's team manager, display only info overview (DONE - need to be tested
    //TODO: retrieve updated match
    //TODO: parse and input players

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

            confirmPlayers(homePlayersText, _homePlayers, true)
            confirmPlayers(awayPlayersText, _awayPlayers, false)

            _isInputOk.value = m_isInputOk
        }
    }

    /**
     * Call this method only if confirmInput() returns true
     */
    fun inputResult(homePlayers: List<User>, awayPlayers: List<User>, ignoreTie: Boolean)
    {
        viewModelScope.launch {
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

            if(calculateScore(match, home1, away1, home2, away2, home3, away3, ignoreTie))
            {
                parsePlayers(homePlayers, awayPlayers)
                val round = match.rounds[round - 1]
                if(round != oldRound)
                {
                    round.edits--
                    MatchManager.addMatch(match)
                    oldRound = round.copy()
                    toast("OK")
                }
            }
        }
    }

    private fun parsePlayers(homePlayers: List<User>, awayPlayers: List<User>)
    {
        val round: Round = match.rounds[round - 1]

        val homePlayersList = homePlayersText.split(" ♢ ")
        val awayPlayersList = awayPlayersText.split(" ♢ ")

        round.homePlayers.clear()
        round.awayPlayers.clear()
        round.homePlayersId.clear()
        round.awayPlayersId.clear()

        homePlayersList.forEach { user ->
            val email = user.removeWhitespaces().split("–").last()
            homePlayers.find { it.email == email }?.let {
                round.homePlayers.add(Player(it.name, it.surname, it.email, it.birthdate, it.sex))
                round.homePlayersId.add(it.id!!)
            }
        }

        awayPlayersList.forEach { user ->
            val email = user.removeWhitespaces().split("–").last()
            awayPlayers.find { it.email == email }?.let {
                round.awayPlayers.add(Player(it.name, it.surname, it.email, it.birthdate, it.sex))
                round.awayPlayersId.add(it.id!!)
            }
        }
    }

    private fun calculateScore(match: Match, home1: Int, away1: Int, home2: Int, away2: Int, home3: Int?, away3: Int?, ignoreTie: Boolean): Boolean
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

        if(!ignoreTie && homeSets == awaySets)
        {
            _isTie.value = true
            return false
        }

        match.rounds[this.round - 1].apply {
            this.homeSets = homeSets
            this.awaySets = awaySets
            this.homeGems = homeGames
            this.awayGems = awayGames
            this.homeGemsSet1 = home1
            this.awayGemsSet1 = away1
            this.homeGemsSet2 = home2
            this.awayGemsSet2 = away2
            this.homeGemsSet3 = home3
            this.awayGemsSet3 = away3
        }
        return true
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

    private fun confirmPlayers(players: Editable, data: MutableLiveData<Boolean>, isHome: Boolean)
    {
        if(players.isEmpty())
        {
            data.value = false
            m_isInputOk = false
        }
        else
        {
            if(round == 3 && players.split(" ♢ ").size != 2)
            {
                data.value = false
                m_isInputOk = false
            }
            else
            {
                if(isHome) homePlayersText = players.toString()
                else awayPlayersText = players.toString()
            }
        }
    }
}
