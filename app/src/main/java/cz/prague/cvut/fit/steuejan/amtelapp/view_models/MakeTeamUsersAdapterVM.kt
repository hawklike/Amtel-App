package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.launch

class MakeTeamUsersAdapterVM : ViewModel()
{
    fun deleteUser(user: User)
    {
        viewModelScope.launch {
            UserManager.deleteUser(user.id!!)
            val team = TeamManager.findTeam(user.teamId!!)
            if(team is ValidTeam)
            {
                val users = team.self.usersId
                users.remove(user.id!!)
                TeamManager.updateTeam(team.self.id!!, mapOf("usersId" to users))
            }
        }
    }
}