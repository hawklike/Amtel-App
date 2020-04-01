package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.states.TeamState
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState

class MatchViewPagerActivityVM : ViewModel()
{
    private val _match = SingleLiveEvent<Match>()

    fun setMatch(match: Match)
    {
        _match.value = match
    }

    val match: LiveData<Match> = _match

    /*---------------------------------------------------*/

    private val _week = SingleLiveEvent<WeekState>()

    fun setWeek(week: WeekState)
    {
        _week.value = week
    }

    val week: LiveData<WeekState> = _week

    /*---------------------------------------------------*/

    private val _homeTeam = SingleLiveEvent<TeamState>()

    fun setHomeTeam(homeTeam: TeamState)
    {
        _homeTeam.value = homeTeam
    }

    val homeTeam: LiveData<TeamState> = _homeTeam

    /*---------------------------------------------------*/

    private val _awayTeam = SingleLiveEvent<TeamState>()

    fun setAwayTeam(awayTeam: TeamState)
    {
        _awayTeam.value = awayTeam
    }

    val awayTeam: LiveData<TeamState> = _awayTeam

    /*---------------------------------------------------*/

    private val _isReport = MutableLiveData<Boolean>()

    fun isReport(isReport: Boolean)
    {
        _isReport.value = isReport
    }

    val isReport: LiveData<Boolean> = _isReport

    /*---------------------------------------------------*/

    private val _page = SingleLiveEvent<Int>()

    fun setPage(page: Int)
    {
        _page.value = page
    }

    val page: LiveData<Int> = _page
}
