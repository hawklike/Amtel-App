package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.TeamToGroupInputter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import kotlinx.coroutines.launch

class TeamsAdapterVM : ViewModel()
{
    fun addToGroup(team: Team, groupName: String)
    {
        viewModelScope.launch {
            TeamToGroupInputter().addToGroup(team, groupName)
        }
    }
}