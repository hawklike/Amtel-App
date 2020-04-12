package cz.prague.cvut.fit.steuejan.amtelapp.view_models.adapters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.UserRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.launch

class ShowTeamPlayersAdapterVM : ViewModel()
{
    fun deleteUser(user: User)
    {
        viewModelScope.launch {
            UserRepository.deleteUser(user.id)
            val team = TeamRepository.findTeam(user.teamId)
            if(team is ValidTeam)
            {
                val users = team.self.users
                val usersId = team.self.usersId
                users.remove(user)
                usersId.remove(user.id!!)
                TeamRepository.updateTeam(team.self.id, mapOf("users" to users, "usersId" to usersId))
            }
        }
    }
}