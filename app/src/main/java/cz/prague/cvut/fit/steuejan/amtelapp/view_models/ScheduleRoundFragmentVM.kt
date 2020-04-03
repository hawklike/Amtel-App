package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.WeekState
import kotlinx.coroutines.launch

class ScheduleRoundFragmentVM : ViewModel()
{
    private val _week = MutableLiveData<WeekState>()
    val week: LiveData<WeekState> = _week

    private lateinit var validWeek: ValidWeek

    /*---------------------------------------------------*/

    fun addWeek(week: String, group: Group, round: Int)
    {
        if(confirmWeek(week))
        {
            viewModelScope.launch {
                val roundDates = group.roundDates
                roundDates[round.toString()] = week.toInt()
                if(GroupManager.updateGroup(group.id, mapOf("roundDates" to roundDates)))
                {
                    _week.value = validWeek
                }
            }
        }
    }

    fun getWeek(group: Group, round: Int)
    {
        viewModelScope.launch {
            val roundDates = group.roundDates
            roundDates[round.toString()]?.let { week ->
                _week.value = ValidWeek(week, DateUtil.getWeekDate(week))
            }
        }
    }

    private fun confirmWeek(weekString: String): Boolean
    {
        val week = WeekState.validate(weekString)
        if(week is InvalidWeek)
        {
            _week.value = week
            return false
        }

        validWeek = week as ValidWeek
        return true
    }

}
