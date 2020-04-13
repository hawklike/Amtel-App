package cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.launch

class EditUserActivityVM : ViewModel()
{
    var user: User? = null
    var team: Team? = null

    var chosenEmail: String? = null
    var emailIdx = -1

    /*---------------------------------------------------*/

    private val _teamLoaded = MutableLiveData<Boolean>()
    val teamLoaded: LiveData<Boolean> = _teamLoaded

    /*---------------------------------------------------*/

    fun getTeam()
    {
        viewModelScope.launch {
            TeamRepository.findTeam(user?.teamId).let {
                if(it is ValidTeam)
                {
                    team = it.self
                    _teamLoaded.value = true
                }
            }
        }
    }
}
