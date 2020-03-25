package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.EmailSender
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.removeWhitespaces
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.*
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.match.MatchInputResultFragment.Companion.COMMA
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.match.MatchInputResultFragment.Companion.EM_DASH
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

    private lateinit var homePlayersText: String
    private lateinit var awayPlayersText: String

    /*---------------------------------------------------*/

    private lateinit var match: Match
    fun setMatch(match: Match)
    {
        this.match = match
    }

    /*---------------------------------------------------*/

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
            confirmGames(thirdHome, _thirdHome, optional = true, isFiftyGroup = isFiftyGroup)
            confirmGames(thirdAway, _thirdAway, optional = true, isFiftyGroup = isFiftyGroup)

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
    fun inputResult(homePlayers: List<User>, awayPlayers: List<User>, isHeadOfLeague: Boolean, ignoreTie: Boolean = false, isReport: Boolean = false)
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
                val (homeUsers, awayUsers) = parsePlayers(homePlayers, awayPlayers)
                addPlayersToMatch(homeUsers, awayUsers)
                if(!isHeadOfLeague) match.edits[round.toString()] = match.edits[round.toString()]!! - 1
                if(!isReport)
                {
                    MatchManager.addMatch(match)
                    _matchAdded.value = match
                }
                else _isReported.value = match
            }
        }
    }

    private fun parsePlayers(homePlayers: List<User>, awayPlayers: List<User>): Pair<List<User>, List<User>>
    {
        val round: Round = match.rounds[round - 1]

        val homePlayersList = homePlayersText.split("$COMMA ")
        val awayPlayersList = awayPlayersText.split("$COMMA ")

        val homeUsers = mutableListOf<User>()
        val awayUsers = mutableListOf<User>()

        homePlayersList.forEach { user ->
            val email = user.removeWhitespaces().split(EM_DASH).last()
            homePlayers.find { it.email == email }?.let {
                round.homePlayers.add(Player(it.id!!, it.name, it.surname, it.email, it.birthdate, it.sex, true))
                homeUsers.add(it)
            }
        }

        awayPlayersList.forEach { user ->
            val email = user.removeWhitespaces().split(EM_DASH).last()
            awayPlayers.find { it.email == email }?.let {
                round.awayPlayers.add(Player(it.id!!, it.name, it.surname, it.email, it.birthdate, it.sex, false))
                awayUsers.add(it)
            }
        }

        return Pair(homeUsers, awayUsers)
    }

    private fun addPlayersToMatch(homePlayers: List<User>, awayPlayers: List<User>)
    {
        when(round)
        {
            1 -> {
                match.usersId[0] = homePlayers.first().id
                match.usersId[1] = awayPlayers.first().id
            }

            2 -> {
                match.usersId[2] = homePlayers.first().id
                match.usersId[3] = homePlayers.last().id
                match.usersId[4] = awayPlayers.first().id
                match.usersId[5] = awayPlayers.last().id
            }

            3 -> {
                match.usersId[6] = homePlayers.first().id
                match.usersId[7] = homePlayers.last().id
                match.usersId[8] = awayPlayers.first().id
                match.usersId[9] = awayPlayers.last().id
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

        match.rounds[this.round - 1] = Round(homeSets, awaySets, homeGames, awayGames, home1, away1, home2, away2, home3, away3)
        setMatchScore(homeSets, awaySets)
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

    private fun confirmPlayers(players: Editable, data: MutableLiveData<Boolean>, isHome: Boolean)
    {
        if(players.isEmpty())
        {
            data.value = false
            m_isInputOk = false
        }
        else
        {
            if(round == 3 && players.split("$COMMA ").size != 2)
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

    fun sendEmail(homeTeam: Team, awayTeam: Team, sets: CharSequence, games: CharSequence, user: String)
    {
        viewModelScope.launch {
            val homeManagerEmail = homeTeam.users.find { it.role.toRole() == UserRole.TEAM_MANAGER }?.email
            val awayManagerEmail = awayTeam.users.find {it.role.toRole() == UserRole.TEAM_MANAGER}?.email

            when(user)
            {
                homeTeam.tmId -> {
                    val subject = "Byl zapsán výsledek $round. zápasu v utkání ${homeTeam.name}–${awayTeam.name} (skupina ${match.group})"

                    val message = String.format(
                        context.getString(R.string.match_input_email),
                        homeTeam.name,
                        round,
                        homeTeam.name,
                        awayTeam.name,
                        match.dateAndTime?.toMyString() ?: context.getString(R.string.unspecified),
                        sets.toString().removeWhitespaces(),
                        games,
                        homePlayersText,
                        awayPlayersText
                    )

                    awayManagerEmail?.let { EmailSender.sendEmail(it, subject, message) }
                    EmailSender.headOfLeagueEmail?.let { EmailSender.sendEmail(it, subject, message) }
                }

                awayTeam.tmId -> {
                    val subject = "Byla podána námitka k výsledku $round. zápasu v utkání ${homeTeam.name}–${awayTeam.name} (skupina ${match.group})"

                    val message = """
                    Dobrý den,
                    
                    vedoucí týmu ${awayTeam.name} právě podal námitku k $round. zápasu v utkání ${homeTeam.name}–${awayTeam.name} ze dne ${match.dateAndTime?.toMyString() ?: "nespecifikováno"}.
                    
                    Dle něj je správný stav zápasu následující:
                    Skóre: ${sets.toString().removeWhitespaces()} na sety a $games na gemy.
                    Hráči: $homePlayersText a $awayPlayersText
                    
                    Na tento email prosím neodpovídejte.
                    
                    Administrátor aplikace AMTEL Opava
                    """.trimIndent()

                    homeManagerEmail?.let { EmailSender.sendEmail(it, subject, message) }
                    EmailSender.headOfLeagueEmail?.let { EmailSender.sendEmail(it, subject, message) }
                }

                else -> {
                    val subject = "Byl zapsán výsledek $round. zápasu v utkání ${homeTeam.name}–${awayTeam.name} (skupina ${match.group})"

                    val message = String.format(
                        context.getString(R.string.match_input_email_headOfLeague),
                        round,
                        homeTeam.name,
                        awayTeam.name,
                        match.dateAndTime?.toMyString() ?: context.getString(R.string.unspecified),
                        sets.toString().removeWhitespaces(),
                        games,
                        homePlayersText,
                        awayPlayersText
                    )

                    homeManagerEmail?.let { EmailSender.sendEmail(it, subject, message) }
                    awayManagerEmail?.let { EmailSender.sendEmail(it, subject, message) }
                }
            }
        }

    }
}
