package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.launch

class MatchArrangementActivityVM : ViewModel()
{
    private val _teams = SingleLiveEvent<Pair<Team, Team>>()
    val teams: LiveData<Pair<Team, Team>> = _teams

    /*---------------------------------------------------*/

    fun getTeams(match: Match)
    {
        viewModelScope.launch {
            val home = TeamManager.findTeam(match.homeId)
            val away = TeamManager.findTeam(match.awayId)

            if(home is ValidTeam && away is ValidTeam)
                _teams.value = Pair(home.self, away.self)
        }
    }
}
