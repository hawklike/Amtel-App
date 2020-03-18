package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.RankingSolver
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.RankingOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeams
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RankingFragmentVM : ViewModel()
{
    private val _teams = SingleLiveEvent<List<Team>>()
    val teams: LiveData<List<Team>> = _teams

    /*---------------------------------------------------*/

    fun loadTeams(group: String, year: Int, orderBy: RankingOrderBy)
    {
        viewModelScope.launch {
            val teams = TeamManager.retrieveTeamsInSeason(group, year)
            if(teams is ValidTeams)
            {
                withContext(Default) {
                    val sortedTeams = RankingSolver(teams.self, year).withOrder(orderBy).sort()
                }
                _teams.value = teams.self
            }
        }
    }
}
