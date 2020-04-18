package cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities

import android.text.Editable
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.MatchRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.MessageRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.*
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Message
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.MatchResult
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toDayInWeek
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState
import kotlinx.coroutines.launch
import java.util.*

class MatchArrangementActivityVM : ViewModel()
{


    /*---------------------------------------------------*/

    private val _teams = SingleLiveEvent<Pair<Team, Team>>()
    val teams: LiveData<Pair<Team, Team>> = _teams

    /*---------------------------------------------------*/

    private val _date = MutableLiveData<Date?>()
    val date: LiveData<Date?> = _date

    /*---------------------------------------------------*/

    private val _match = MutableLiveData<Match>()
    val match: LiveData<Match> = _match

    fun setMatch(match: Match)
    {
        _match.value = match
    }

    /*---------------------------------------------------*/

    fun getTeams(week: WeekState)
    {
        viewModelScope.launch {
            val home = TeamRepository.findTeam(match.value?.homeId)
            val away = TeamRepository.findTeam(match.value?.awayId)

            if(home is ValidTeam && away is ValidTeam)
            {
                _teams.value = Pair(home.self, away.self)
                match.value?.dateAndTime?.let { _date.value = it }
                    ?: findBestDate(home.self, away.self, week, match.value!!)
            }
        }
    }

    private fun findBestDate(homeTeam: Team, awayTeam: Team, week: WeekState, match: Match)
    {
        viewModelScope.launch {
            if(week is ValidWeek)
            {
                val homeDays = homeTeam.playingDays.map { it.toDayInWeek() }
                val awayDays = awayTeam.playingDays.map { it.toDayInWeek() }

                _date.value = DateUtil.findDate(homeDays, awayDays, week.range)?.setTime(16, 0)
                _date.value?.let { dateAndTime ->
                    MatchRepository.updateMatch(match.id!!, mapOf("dateAndTime" to dateAndTime))
                    _match.value?.dateAndTime = dateAndTime
                    sendEmailPlaceAndDate(dateAndTime = dateAndTime, place = match.place?.let { it } ?: homeTeam.place)
                }
            }
        }
    }

    fun sendMessage(fullname: String, messageText: String, matchId: String?)
    {
        viewModelScope.launch {
            MessageRepository.addMessage(Message(fullname, messageText, AuthManager.currentUser?.uid ?: ""), matchId, true)
        }
    }

    fun setDialogDate(date: Editable): Calendar?
    {
        return if(date.isEmpty()) null
        else date.toString().toCalendar()
    }

    fun updateDateTime(date: Calendar)
    {
        _date.value = date.time
        viewModelScope.launch {
            if(MatchRepository.updateMatch(match.value?.id, mapOf("dateAndTime" to date.time)))
            {
                toast(context.getString(R.string.match_dateTime_change_success), length = Toast.LENGTH_LONG)
                _match.value?.dateAndTime = date.time
                sendEmailPlaceAndDate(dateAndTime = date.time)
            }
        }
    }

    fun updatePlace(place: String)
    {
        if(place != match.value?.place ?: "")
        {
            viewModelScope.launch {
                if(MatchRepository.updateMatch(match.value?.id, mapOf("place" to place)))
                {
                    toast(context.getString(R.string.match_place_change_success), length = Toast.LENGTH_LONG)
                    _match.value?.place = place
                    sendEmailPlaceAndDate(place = place)
                }
            }
        }
    }

    fun initPlace()
    {
        match.value?.place ?: viewModelScope.launch {
            MatchRepository.updateMatch(match.value?.id, mapOf("place" to teams.value?.first?.place))
        }
    }

    fun countTotalScore(match: Match): MatchResult
    {
        var homeScore = 0
        var awayScore = 0

        match.rounds.forEach {
            if(it.homeWinner == true) homeScore++
            else if(it.homeWinner == false) awayScore++
        }

        return MatchResult(homeScore, awayScore)
    }

    private fun sendEmailPlaceAndDate(dateAndTime: Date? = null, place: String? = null)
    {
        viewModelScope.launch {
            val homeManagerEmail = teams.value?.first?.users?.find { it.role.toRole() == UserRole.TEAM_MANAGER }?.email
            val awayManagerEmail = teams.value?.second?.users?.find {it.role.toRole() == UserRole.TEAM_MANAGER}?.email

            val match = _match.value ?: return@launch

            val subject = "Bylo nastaveno ${place?.let { "místo " } ?: ""}${if(dateAndTime != null && place != null) "a " else ""}${dateAndTime?.let { "datum " } ?: ""}utkání ${match.home}–${match.away} ve skupině ${match.groupName}"

            val message = """
                Dobrý den,
                
                právě bylo v aplikaci nastaveno ${place?.let { "místo " } ?: ""}${if(dateAndTime != null && place != null) "a " else ""}${dateAndTime?.let { "datum " } ?: ""}utkání ${match.home}–${match.away} ve skupině ${match.groupName}.
                
                Místo: ${match.place} ${place?.let { "<---" } ?: ""}
                Datum a čas: ${match.dateAndTime?.toMyString("dd.MM.yyyy 'v' HH:mm") ?: "nespecifikováno"} ${dateAndTime?.let { "<---" } ?: ""}
                
                Na tento email prosím neodpovídejte.
                                    
                Administrátor aplikace AMTEL Opava
                """.trimIndent()

            homeManagerEmail?.let { EmailSender.sendEmail(it, subject, message) }
            awayManagerEmail?.let { EmailSender.sendEmail(it, subject, message) }
        }
    }

    fun defaultEndGame(match: Match, isHomeWinner: Boolean, homeTeam: Team, awayTeam: Team): Match
    {
        if(isHomeWinner) setDefaultResult(match, 6, 0)
        else setDefaultResult(match, 0, 6)

        match.rounds.forEach {
            it.homePlayers = mutableListOf()
            it.awayPlayers = mutableListOf()
        }

        viewModelScope.launch {
            sendDefaultResultEmail(match, homeTeam, awayTeam)
        }

        return match
    }

    private fun sendDefaultResultEmail(match: Match, homeTeam: Team, awayTeam: Team)
    {
        viewModelScope.launch {
            val subject = "Byla zvolena kontumační prohra/výhra v utkání ${homeTeam.name}–${awayTeam.name} (skupina ${match.groupName})"

            val message = """
                    Dobrý den,
                    
                    vedoucí týmu ${homeTeam.name} právě zvolil kontumační prohru/výhru v utkání ${homeTeam.name}–${awayTeam.name} ze dne ${match.dateAndTime?.toMyString() ?: "nespecifikováno"}.
                    
                    Kontumačně vyhrál tým: ${if(match.homeScore!! > match.awayScore!!) homeTeam.name else awayTeam.name}
                    Tým ${if(match.homeScore!! < match.awayScore!!) homeTeam.name else awayTeam.name} se rozhodl do utkání nenastoupit a kontumačně prohrál.
                    
                    Na tento email prosím neodpovídejte.
                    
                    Administrátor aplikace AMTEL Opava
                    """.trimIndent()

            val awayManagerEmail = teams.value?.second?.users?.find {it.role.toRole() == UserRole.TEAM_MANAGER}?.email
            awayManagerEmail?.let { EmailSender.sendEmail(it, subject, message) }
            EmailSender.headOfLeagueEmail?.let { EmailSender.sendEmail(it, subject, message) }
        }
    }

    private fun setDefaultResult(match: Match, homePoints: Int, awayPoints: Int)
    {
        match.rounds.forEach {
            it.homeGemsSet1 = homePoints
            it.homeGemsSet2 = homePoints
            it.awayGemsSet1 = awayPoints
            it.awayGemsSet2 = awayPoints

            it.homeGems = 2 * homePoints
            it.awayGems = 2* awayPoints

            if(homePoints > awayPoints)
            {
                it.homeWinner = true
                it.homeSets = 2
                it.awaySets = 0
            }
            else
            {
                it.homeWinner = false
                it.homeSets = 0
                it.awaySets = 2
            }
        }
    }

    fun sendEmail(lastUpdated: Date?, match: Match, homeTeam: Team, awayTeam: Team)
    {
        viewModelScope.launch {
            if(DateUtil.compareDatesWithTime(lastUpdated, match.lastUpdate) != 0)
            {
                val homeManagerEmail = homeTeam.users.find { it.role.toRole() == UserRole.TEAM_MANAGER }?.email
                val awayManagerEmail = awayTeam.users.find { it.role.toRole() == UserRole.TEAM_MANAGER }?.email

                val subject = "Byl zapsán výsledek utkání ${homeTeam.name}–${awayTeam.name} (skupina ${match.groupName})"

                when(AuthManager.currentUser!!.uid)
                {
                    homeTeam.tmId -> {
                        val message = getEmailText(match, homeTeam, awayTeam, "Vedoucí týmu ${homeTeam.name}")
                        awayManagerEmail?.let { EmailSender.sendEmail(it, subject, message, BackgroundMail.TYPE_HTML) }
                        EmailSender.headOfLeagueEmail?.let { EmailSender.sendEmail(it, subject, message, BackgroundMail.TYPE_HTML) }
                    }
                    awayTeam.tmId -> {}
                    else -> {
                        val message = getEmailText(match, homeTeam, awayTeam, "Vedoucí soutěže")
                        homeManagerEmail?.let { EmailSender.sendEmail(it, subject, message, BackgroundMail.TYPE_HTML) }
                        awayManagerEmail?.let { EmailSender.sendEmail(it, subject, message, BackgroundMail.TYPE_HTML) }
                    }
                }
            }
        }
    }

    private fun getEmailText(match: Match, homeTeam: Team, awayTeam: Team, author: String): String
    {
        return """
            <p>Dobrý den,</p>
            <p>$author právě zadal do aplikace výsledek utkání <strong>${homeTeam.name}–${awayTeam.name}</strong> ze dne ${match.dateAndTime?.toMyString() ?: "nespecifikováno"}.</p>
            
            <h3>Výsledné skóre je následující:</h3>
            
            ${homeTeam.name} (domácí) <br>
            <strong>${match.homeScore}:${match.awayScore}</strong> <br>
            ${awayTeam.name} (hosté) <br>
            
            <h3>Podrobnosti:</h3>
            
            <p>${getScore(match)}</p>
            
            <h3>Hráči:</h3>
            
            <p>${getPlayers(match)}</p>
            
            <p>Na tento email prosím neodpovídejte.</p>
            
            <p>Administrátor aplikace AMTEL Opava</p>
        """.trimIndent()
    }

    private fun getScore(match: Match): String
    {
        val firstRound = MatchRepository.getResults(match.rounds[0])
        val secondRound = MatchRepository.getResults(match.rounds[1])
        val thirdRound = MatchRepository.getResults(match.rounds[2])

        return """
            1. zápas: ${firstRound.sets} na sety a ${firstRound.games} na gemy <br>
            2. zápas: ${secondRound.sets} na sety a ${secondRound.games} na gemy <br>
            3. zápas: ${thirdRound.sets} na sety a ${thirdRound.games} na gemy <br>
        """.trimIndent()
    }

    private fun getPlayers(match: Match): String
    {
        val firstRound = with(match.rounds[0]) {
            "${homePlayers.joinToString(", ")} a ${awayPlayers.joinToString(", ")}"
        }
        val secondRound = with(match.rounds[1]) {
            "${homePlayers.joinToString(", ")} a ${awayPlayers.joinToString(", ")}"
        }
        val thirdRound = with(match.rounds[2]) {
            "${homePlayers.joinToString(", ")} a ${awayPlayers.joinToString(", ")}"
        }

        return """
            1. zápas: $firstRound <br>
            2. zápas: $secondRound <br>
            3. zápas: $thirdRound  <br>
        """.trimIndent()
    }

}
