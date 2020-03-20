package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch

class ManageGroupsActivityVM : ViewModel()
{
    private val _week = MutableLiveData<WeekState>()
    val week: LiveData<WeekState> = _week

    /*---------------------------------------------------*/

    fun setPlayOff(week: String)
    {
        if(confirmWeek(week))
        {
            viewModelScope.launch {
                val roundDates = mutableMapOf("playOff" to week.toInt())
                if(GroupManager.addGroup(Group("Baráž", roundDates = roundDates, playingPlayOff = false, playOff = true)) is ValidGroup)
                {
                    toast("Baráž úspěšně připravena.")
                }
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
        return true
    }

    fun getPlayOffWeek()
    {
        viewModelScope.launch {
            val groups = GroupManager.findGroups("playOff", true)
            if(groups is ValidGroups && groups.self.isNotEmpty())
            {
                val playOff = groups.self.first()
                val week = playOff.roundDates["playOff"]
                week?.let { _week.value = ValidWeek(week, emptyList()) }
            }
        }
    }

}