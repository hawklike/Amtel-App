package cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.UserRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.launch

class PlayersFragmentVM : ViewModel()
{
    var orderBy = UserOrderBy.SURNAME
    var query = UserRepository.retrieveAllUsers()

    /*---------------------------------------------------*/

    private val _userDeleted = SingleLiveEvent<Boolean>()
    val isUserDeleted: LiveData<Boolean> = _userDeleted

    /*---------------------------------------------------*/

    fun deleteUser(user: User?)
    {
        if(user == null) return
        viewModelScope.launch {
            _userDeleted.value = UserRepository.deleteUser(user.id)
            val team = TeamRepository.findTeam(user.teamId)
            if(team is ValidTeam)
            {
                val users = team.self.users
                val usersId = team.self.usersId
                users.removeAll { it == user }
                usersId.removeAll { it == user.id }
                TeamRepository.updateTeam(team.self.id, mapOf("users" to users, "usersId" to usersId))
            }
        }
    }
}
