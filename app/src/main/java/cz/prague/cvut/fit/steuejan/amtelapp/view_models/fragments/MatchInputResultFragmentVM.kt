package cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.EmailSender
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.removeWhitespaces
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.*
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.MatchRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.UserRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidSet
import cz.prague.cvut.fit.steuejan.amtelapp.states.SetState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidSet
import kotlinx.coroutines.launch
import java.util.*

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

    private val _matchAdded = SingleLiveEvent<Match>()
    val matchAdded: LiveData<Match> = _matchAdded

    /*---------------------------------------------------*/

    private val _isReported = SingleLiveEvent<Match>()
    val isReported: LiveData<Match> = _isReported

    /*---------------------------------------------------*/

    private val _isTie = SingleLiveEvent<Boolean>()
    val isTie: LiveData<Boolean> = _isTie

    /*---------------------------------------------------*/

    private var m_isInputOk = true
    var round: Int = 1

    var homePlayersBefore = listOf<Player>()
    var awayPlayersBefore = listOf<Player>()

    var mHomePlayers: List<Player> = listOf()
    var mAwayPlayers: List<Player> = listOf()

    var selectedHomePlayers: MutableList<Int> = mutableListOf()
    var selectedAwayPlayers: MutableList<Int> = mutableListOf()

    /*---------------------------------------------------*/

    private lateinit var match: Match
    fun setMatch(match: Match)
    {
        this.match = match
    }

    /*---------------------------------------------------*/

    fun setSelectedPlayers(homeTeam: Team, awayTeam: Team)
    {
        homeTeam.users.forEachIndexed { index, user ->
            mHomePlayers.find { it.playerId == user.id}?.let {
                selectedHomePlayers.add(index)
            }
        }

        awayTeam.users.forEachIndexed { index, user ->
            mAwayPlayers.find { it.playerId == user.id}?.let {
                selectedAwayPlayers.add(index)
            }
        }
    }

    fun handleListItemSingleChoice(team: Team, selectedIndex: Int, homeTeam: Boolean)
    {
        if(homeTeam)
        {
            selectedHomePlayers = mutableListOf(selectedIndex)
            mHomePlayers = listOf(team.users[selectedIndex].toPlayer())
        }
        else
        {
            selectedAwayPlayers = mutableListOf(selectedIndex)
            mAwayPlayers = listOf(team.users[selectedIndex].toPlayer())
        }
    }

    //this method sets selected players in the dialog
    fun handleListItemMultiChoice(team: Team, selectedIndices: IntArray, homeTeam: Boolean)
    {
        val queue = ArrayDeque<Player>()
        team.users.forEachIndexed { index, user ->
            if(selectedIndices.contains(index)) queue.add(user.toPlayer())
        }

        if(homeTeam)
        {
            selectedHomePlayers = selectedIndices.toMutableList()
            mHomePlayers = queue.toList()
        }
        else
        {
            selectedAwayPlayers = selectedIndices.toMutableList()
            mAwayPlayers = queue.toList()
        }
    }

    fun confirmInput(firstHome: String, firstAway: String, secondHome: String, secondAway: String, thirdHome: String, thirdAway: String, isFiftyGroup: Boolean)
    {
        viewModelScope.launch {
            m_isInputOk = true

            confirmGames(firstHome, _firstHome)
            confirmGames(firstAway, _firstAway)
            confirmGames(secondHome, _secondHome)
            confirmGames(secondAway, _secondAway)
            confirmGames(thirdHome, _thirdHome, optional = true, isFiftyGroup = isFiftyGroup)
            confirmGames(thirdAway, _thirdAway, optional = true, isFiftyGroup = isFiftyGroup)

            confirmSet(_firstHome, _firstAway)
            confirmSet(_secondHome, _secondAway)
            if(!isFiftyGroup) confirmSet(_thirdHome, _thirdAway)

            confirmPlayers(mHomePlayers, _homePlayers, isFiftyGroup)
            confirmPlayers(mAwayPlayers, _awayPlayers, isFiftyGroup)

            _isInputOk.value = m_isInputOk
        }
    }

    fun inputResult(isHeadOfLeague: Boolean, ignoreTie: Boolean = false, isReport: Boolean = false)
    {
        viewModelScope.launch {
            val home1: Int = (firstHome.value as ValidSet).self
            val home2: Int = (secondHome.value as ValidSet).self
            val home3: Int? = with((thirdHome.value as ValidSet).self) {
                if(this == Int.MAX_VALUE) null //last set not input
                else this
            }

            val away1: Int = (firstAway.value as ValidSet).self
            val away2: Int = (secondAway.value as ValidSet).self
            val away3: Int? = with((thirdAway.value as ValidSet).self) {
                if(this == Int.MAX_VALUE) null //last set not input
                else this
            }

            if(calculateScore(match, home1, away1, home2, away2, home3, away3, ignoreTie))
            {
                addPlayers()
                if(!isHeadOfLeague) match.edits[round.toString()] = match.edits[round.toString()]!! - 1 //decrease number of free edits
                if(!isReport)
                {
                    match.lastUpdate = Date()
                    //update match in database
                    MatchRepository.setMatch(match)
                    updatePlayers()
                    _matchAdded.value = match
                }
                else _isReported.value = match
            }
        }
    }

    private suspend fun updatePlayers()
    {
        val roundNumber = round
        val round = match.rounds[roundNumber - 1]

        val before = homePlayersBefore + awayPlayersBefore
        val now = mHomePlayers + mAwayPlayers

        //players input for the first time
        if(before.isEmpty())
        {
            now.forEach {
                //update in database
                UserRepository.addRound(it.playerId, match.id!!, round, roundNumber)
            }
        }
        else if(before.size == now.size)
        {
            now.forEachIndexed { index, nowUser ->
                //player was changed
                if(nowUser.playerId != before[index].playerId)
                    //delete round from a previous user
                    UserRepository.deleteRound(before[index].playerId, match.id!!, roundNumber)

                // add/update round to a current user
                UserRepository.addRound(nowUser.playerId, match.id!!, round, roundNumber)
            }
        }
    }

    //add players to a round
    private fun addPlayers()
    {
        val round: Round = match.rounds[round - 1]

        mHomePlayers.forEach {
            round.homePlayers.add(Player(it.playerId, it.name, it.surname, it.email, it.birthdate, it.sex, true))
        }

        mAwayPlayers.forEach {
            round.awayPlayers.add(Player(it.playerId, it.name, it.surname, it.email, it.birthdate, it.sex, false))
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
            //warn a user that the result is tied
            _isTie.value = true
            return false
        }

        //insert round into a match (one match consists of three rounds)
        match.rounds[round - 1] = Round(homeSets, awaySets, homeGames, awayGames, home1, away1, home2, away2, home3, away3, matchId = match.id!!, round = round)
        setMatchScore(homeSets, awaySets)
        //everything ok
        return true
    }

    private fun setMatchScore(homeSets: Int, awaySets: Int)
    {
        val round =  match.rounds[round - 1]
        when
        {
            homeSets > awaySets -> round.homeWinner = true
            homeSets < awaySets -> round.homeWinner = false
            else -> round.homeWinner = null
        }
    }

    private fun confirmSet(home: MutableLiveData<SetState>, away: MutableLiveData<SetState>)
    {
        if(home.value is ValidSet && away.value is ValidSet)
        {
            val homeGames = (home.value as ValidSet).self
            val awayGames = (away.value as ValidSet).self

            //games are invalid
            if(homeGames == Int.MAX_VALUE && awayGames == Int.MAX_VALUE) return

            if(!SetState.validate(homeGames, awayGames))
            {
                m_isInputOk = false
                home.value = InvalidSet(context.getString(R.string.games_invalid_rules_error))
                away.value = InvalidSet(context.getString(R.string.games_invalid_rules_error))
            }
        }
    }

    private fun confirmGames(games: String, data: MutableLiveData<SetState>, optional: Boolean = false, isFiftyGroup: Boolean = false)
    {
        with(SetState.validate(games, optional, isFiftyGroup)) {
            if(this is InvalidSet) m_isInputOk = false
            data.value = this
        }
    }

    private fun confirmPlayers(players: List<Player>, data: MutableLiveData<Boolean>, fiftyGroup: Boolean)
    {
        if(players.isEmpty())
        {
            data.value = false
            m_isInputOk = false
        }
        else
        {
            //50+ group plays two double matches, other groups play only one double
            if((!fiftyGroup && round == 3 && players.size != 2) || (fiftyGroup && (round == 2 || round == 3) && players.size != 2))
            {
                data.value = false
                m_isInputOk = false
            }
        }
    }

    //sends email only when a match is reported
    fun sendEmail(homeTeam: Team, awayTeam: Team, sets: CharSequence, games: CharSequence)
    {
        viewModelScope.launch {
            val homeManagerEmail = homeTeam.users.find { it.role.toRole() == UserRole.TEAM_MANAGER }?.email
            awayTeam.users.find { it.role.toRole() == UserRole.TEAM_MANAGER }?.email

            if(AuthManager.currentUser?.uid == awayTeam.tmId)
            {
                val subject = "Byla podána námitka k výsledku $round. zápasu v utkání ${homeTeam.name}–${awayTeam.name} (skupina ${match.groupName})"

                val message = """
                    Dobrý den,
                    
                    vedoucí týmu ${awayTeam.name} právě podal námitku k $round. zápasu v utkání ${homeTeam.name}–${awayTeam.name} ze dne ${match.dateAndTime?.toMyString() ?: "nespecifikováno"}.
                    
                    Dle něj je správný stav zápasu následující:
                    Skóre: ${sets.toString().removeWhitespaces()} na sety a $games na gemy.
                    Hráči: ${mHomePlayers.joinToString(", ")} a ${mAwayPlayers.joinToString(", ")}
                    
                    Na tento email prosím neodpovídejte.
                    
                    Administrátor aplikace AMTEL Opava
                    """.trimIndent()

                homeManagerEmail?.let { EmailSender.sendEmail(it, subject, message) }
                EmailSender.headOfLeagueEmail?.let { EmailSender.sendEmail(it, subject, message) }
            }
        }

    }
}
