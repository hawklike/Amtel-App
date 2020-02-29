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
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.setTime
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toCalendar
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toDayInWeek
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
                MatchManager.updateMatch(match.id!!, mapOf("dateAndTime" to date.value))
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
                toast(context.getString(R.string.match_dateTime_change_success), length = Toast.LENGTH_LONG)
        }
    }

    fun updatePlace(place: String)
    {
        if(place != teams.value?.first?.place ?: "")
        {
            viewModelScope.launch {
                if(MatchManager.updateMatch(match.value?.id, mapOf("place" to place)))
                    toast(context.getString(R.string.match_place_change_success), length = Toast.LENGTH_LONG)
            }
        }
    }

    fun initPlace()
    {
        match.value?.place ?: viewModelScope.launch {
            MatchManager.updateMatch(match.value?.id, mapOf("place" to teams.value?.first?.place))
        }
    }

}
