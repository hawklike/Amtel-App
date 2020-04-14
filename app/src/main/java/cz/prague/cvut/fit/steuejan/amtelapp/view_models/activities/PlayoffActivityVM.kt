package cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Playoff
import cz.prague.cvut.fit.steuejan.amtelapp.states.UserState

class PlayoffActivityVM : ViewModel()
{
    private val _user = SingleLiveEvent<UserState>()

    fun setUser(user: UserState)
    {
        _user.value = user
    }

    val user: LiveData<UserState> = _user

    /*---------------------------------------------------*/

    private val _playoff = SingleLiveEvent<Playoff?>()

    fun setPlayoff(playoff: Playoff?)
    {
        _playoff.value = playoff
    }

    val playoff: LiveData<Playoff?> = _playoff
}
