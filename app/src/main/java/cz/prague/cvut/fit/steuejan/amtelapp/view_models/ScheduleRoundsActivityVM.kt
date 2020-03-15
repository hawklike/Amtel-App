package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.states.UserState

class ScheduleRoundsActivityVM : ViewModel()
{
    private val _user = SingleLiveEvent<UserState>()

    fun setUser(user: UserState)
    {
        _user.value = user
    }

    val user: LiveData<UserState> = _user

}
