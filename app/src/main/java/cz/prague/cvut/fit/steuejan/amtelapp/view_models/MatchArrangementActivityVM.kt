package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
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

    fun getTeams(match: Match)
    {
        viewModelScope.launch {
            val home = TeamManager.findTeam(match.homeId)
            val away = TeamManager.findTeam(match.awayId)

            if(home is ValidTeam && away is ValidTeam)
                _teams.value = Pair(home.self, away.self)
        }
    }

    fun findBestDate(homeTeam: Team, awayTeam: Team, week: WeekState)
    {
        if(week is ValidWeek)
        {
            viewModelScope.launch {
                val homeDays = homeTeam.playingDays.map { it.toDayInWeek() }
                val awayDays = awayTeam.playingDays.map { it.toDayInWeek() }

                _date.value = DateUtil.findDate(homeDays, awayDays, week.range)
            }
        }
    }

    fun setDialogDate(date: Editable): Calendar?
    {
        return if(date.isEmpty()) null
        else date.toString().toCalendar()
    }
}
