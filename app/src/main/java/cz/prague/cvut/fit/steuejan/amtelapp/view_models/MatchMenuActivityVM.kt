package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState

class MatchMenuActivityVM : ViewModel()
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
}
