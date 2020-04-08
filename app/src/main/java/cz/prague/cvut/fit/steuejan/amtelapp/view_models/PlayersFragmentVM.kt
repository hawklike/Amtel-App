package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.launch

class PlayersFragmentVM : ViewModel()
{
    var orderBy = UserOrderBy.SURNAME
    var query = UserManager.retrieveAllUsers()

    /*---------------------------------------------------*/

    private val _userDeleted = SingleLiveEvent<Boolean>()
    val isUserDeleted: LiveData<Boolean> = _userDeleted

    /*---------------------------------------------------*/

    fun deleteUser(user: User?)
    {
        if(user == null) return
        viewModelScope.launch {
            _userDeleted.value = UserManager.deleteUser(user.id)
            val team = TeamManager.findTeam(user.teamId)
            if(team is ValidTeam)
            {
                val users = team.self.users
                val usersId = team.self.usersId
                users.removeAll { it == user }
                usersId.removeAll { it == user.id }
                TeamManager.updateTeam(team.self.id, mapOf("users" to users, "usersId" to usersId))
            }
        }
    }

    fun editUser(user: User?)
    {
        //TODO
    }
}
