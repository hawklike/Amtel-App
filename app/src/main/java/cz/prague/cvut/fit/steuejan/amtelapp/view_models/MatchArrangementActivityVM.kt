package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.text.Editable
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun countTotalScore(match: Match)
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
                    updatePoints(match)
                }
            }
        }
    }

    private suspend fun updatePoints(match: Match)
    {
        val homeTeam = teams.value?.first
        val awayTeam = teams.value?.second

        homeTeam?.let {
            updatePoints(it, match) { match.homeScore!! > match.awayScore!! }
        }

        awayTeam?.let {
            updatePoints(it, match) { match.awayScore!! > match.homeScore!! }
        }
    }

    private suspend fun updatePoints(team: Team, match: Match, predicate: () -> Boolean)
    {
        val year = DateUtil.actualYear.toString()

        val pointsPerYear = team.pointsPerMatch[year]
        if(pointsPerYear == null) team.pointsPerMatch[year] = mutableMapOf()

        var sum = 0
        team.pointsPerMatch[year]?.let { points ->
            points[match.id!!] = if(predicate.invoke()) 2 else 1
            sum = points.values.sum()
        }

        team.pointsPerYear[year] = sum
        TeamManager.addTeam(team)
    }


    //TODO: add to string resources
    private fun sendEmail(dateAndTime: Date? = null, place: String? = null)
    {
        val homeManagerEmail = teams.value?.first?.users?.find { it.role.toRole() == UserRole.TEAM_MANAGER }?.email
        val awayManagerEmail = teams.value?.second?.users?.find {it.role.toRole() == UserRole.TEAM_MANAGER}?.email

        val match = _match.value ?: return

        val subject = "Bylo nastaveno ${place?.let { "místo " } ?: ""}${if(dateAndTime != null && place != null) "a " else ""}${dateAndTime?.let { "datum " } ?: ""}utkání ${match.home}–${match.away} ve skupině ${match.group}"

        val message = """
        Dobrý den,
        
        právě bylo v aplikaci nastaveno ${place?.let { "místo " } ?: ""}${if(dateAndTime != null && place != null) "a " else ""}${dateAndTime?.let { "datum " } ?: ""}utkání ${match.home}–${match.away} ve skupině ${match.group}.
        
        Místo: ${match.place} ${place?.let { "<---" } ?: ""}
        Datum a čas: ${match.dateAndTime?.toMyString("dd.MM.yyyy 'v' HH:mm") ?: "nespecifikováno"} ${dateAndTime?.let { "<---" } ?: ""}
        
        Na tento email prosím neodpovídejte.
                            
        Administrátor aplikace AMTEL Opava
        """.trimIndent()

        homeManagerEmail?.let { EmailSender.sendEmail(it, subject, message)}
        awayManagerEmail?.let { EmailSender.sendEmail(it, subject, message)}
    }

}
