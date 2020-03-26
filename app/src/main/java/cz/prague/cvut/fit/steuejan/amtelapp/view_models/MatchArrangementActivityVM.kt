package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.text.Editable
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.POINTS_DEFAULT_LOSS
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.POINTS_LOOSE
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.POINTS_WIN
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.*
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
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
            val home = TeamManager.findTeam(match.value?.homeId)
            val away = TeamManager.findTeam(match.value?.awayId)

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

                _date.value = DateUtil.findDate(homeDays, awayDays, week.range)?.setTime(12, 0)
                _date.value?.let { dateAndTime ->
                    MatchManager.updateMatch(match.id!!, mapOf("dateAndTime" to dateAndTime))
                    _match.value?.dateAndTime = dateAndTime
                    sendEmail(dateAndTime = dateAndTime, place = match.place?.let { it } ?: homeTeam.place)
                }
            }
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
            if(MatchManager.updateMatch(match.value?.id, mapOf("dateAndTime" to date.time)))
            {
                toast(context.getString(R.string.match_dateTime_change_success), length = Toast.LENGTH_LONG)
                _match.value?.dateAndTime = date.time
                sendEmail(dateAndTime = date.time)
            }
        }
    }

    fun updatePlace(place: String)
    {
        if(place != match.value?.place ?: "")
        {
            viewModelScope.launch {
                if(MatchManager.updateMatch(match.value?.id, mapOf("place" to place)))
                {
                    toast(context.getString(R.string.match_place_change_success), length = Toast.LENGTH_LONG)
                    _match.value?.place = place
                    sendEmail(place = place)
                }
            }
        }
    }

    fun initPlace()
    {
        match.value?.place ?: viewModelScope.launch {
            MatchManager.updateMatch(match.value?.id, mapOf("place" to teams.value?.first?.place))
        }
    }

    fun countTotalScore(match: Match, isDefaultLoss: Boolean = false)
    {
        var homeScore = 0
        var awayScore = 0

        match.rounds.forEach {
            if(it.homeWinner == true) homeScore++
            else if(it.homeWinner == false) awayScore++
        }

        if(homeScore + awayScore == 0)
        {
            if(match.homeScore != null || match.awayScore != null)
            {
                match.homeScore = null
                match.awayScore = null
                viewModelScope.launch {
                     MatchManager.addMatch(match)
                }
            }
        }
        else
        {
            if(match.homeScore != homeScore || match.awayScore != awayScore)
            {
                match.homeScore = homeScore
                match.awayScore = awayScore
                viewModelScope.launch {
                    MatchManager.addMatch(match)
                    updatePoints(match, isDefaultLoss)
                }
            }
        }
    }

    private suspend fun updatePoints(match: Match, isDefaultLoss: Boolean)
    {
        val homeTeam = teams.value?.first
        val awayTeam = teams.value?.second

        homeTeam?.let {
            updatePoints(it, match, isDefaultLoss) { match.homeScore!! > match.awayScore!! }
        }

        awayTeam?.let {
            updatePoints(it, match, isDefaultLoss) { match.awayScore!! > match.homeScore!! }
        }
    }

    private suspend fun updatePoints(team: Team, match: Match, isDefaultLoss: Boolean, isWinner: () -> Boolean)
    {
        val year = DateUtil.actualYear

        val pointsPerYear = team.pointsPerMatch[year]
        if(pointsPerYear == null) team.pointsPerMatch[year] = mutableMapOf()

        var sum = 0
        var wins = 0
        team.pointsPerMatch[year]!!.let { points ->
            points[match.id!!] = when
            {
                isWinner.invoke() -> POINTS_WIN
                isDefaultLoss -> POINTS_DEFAULT_LOSS
                else -> POINTS_LOOSE
            }
            sum = points.values.sum()
            points.values.forEach { if(it == POINTS_WIN) wins++ }
        }

        team.pointsPerYear[year] = sum
        team.winsPerYear[year] = wins
        team.lossesPerYear[year] = team.pointsPerMatch[year]!!.size - wins
        team.matchesPerYear[year] = team.pointsPerMatch[year]!!.size

        initSetsStatistics(team, match)

        TeamManager.addTeam(team)
    }

    private fun initSetsStatistics(team: Team, match: Match)
    {
        val year = DateUtil.actualYear

        val positiveSetsPerYear = team.setsPositivePerMatch[year]
        if(positiveSetsPerYear == null) team.setsPositivePerMatch[year] = mutableMapOf()

        val negativeSetsPerYear = team.setsNegativePerMatch[year]
        if(negativeSetsPerYear == null) team.setsNegativePerMatch[year] = mutableMapOf()

        team.setsPositivePerMatch[year]!!.let { sets ->
            sets[match.id!!] = when(team.id)
            {
                match.homeId -> match.rounds.fold(0) { acc, round -> round.homeSets?.let { acc + it } ?: acc }
                else -> match.rounds.fold(0) { acc, round -> round.awaySets?.let { acc + it } ?: acc }
            }
            team.positiveSetsPerYear[year] = sets.values.sum()
        }

        team.setsNegativePerMatch[year]!!.let { sets ->
            sets[match.id!!] = when(team.id)
            {
                match.homeId -> match.rounds.fold(0) { acc, round -> round.awaySets?.let { acc + it } ?: acc }
                else -> match.rounds.fold(0) { acc, round -> round.homeSets?.let { acc + it } ?: acc }
            }
            team.negativeSetsPerYear[year] = sets.values.sum()
        }
    }


    //TODO: add to string resources
    private fun sendEmail(dateAndTime: Date? = null, place: String? = null)
    {
        val homeManagerEmail = teams.value?.first?.users?.find { it.role.toRole() == UserRole.TEAM_MANAGER }?.email
        val awayManagerEmail = teams.value?.second?.users?.find {it.role.toRole() == UserRole.TEAM_MANAGER}?.email

        val match = _match.value ?: return

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

    fun defaultEndGame(match: Match, isHomeWinner: Boolean, homeTeam: Team, awayTeam: Team)
    {
        if(isHomeWinner) setDefaultResult(match, 6, 0)
        else setDefaultResult(match, 0, 6)
        countTotalScore(match, true)
        sendDefaultResultEmail(match, homeTeam, awayTeam)
    }

    private fun sendDefaultResultEmail(match: Match, homeTeam: Team, awayTeam: Team)
    {
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

}
