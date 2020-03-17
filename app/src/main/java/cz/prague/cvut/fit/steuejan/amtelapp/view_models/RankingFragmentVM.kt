package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeams
import kotlinx.coroutines.launch

class RankingFragmentVM : ViewModel()
{
    private val _teams = SingleLiveEvent<List<Team>>()
    val teams: LiveData<List<Team>> = _teams

    /*---------------------------------------------------*/

    fun loadTeams(group: String, year: Int)
    {
        viewModelScope.launch {
            val teams = TeamManager.retrieveTeamsInSeason(group, year)
            if(teams is ValidTeams) _teams.value = teams.self
        }
    }
}
