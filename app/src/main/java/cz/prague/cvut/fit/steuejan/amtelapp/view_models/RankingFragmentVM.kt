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

    private var teamByPoints: List<Team> = emptyList()

    var orderBy = RankingOrderBy.POINTS
    private set

    private var mTeams: List<Team> = emptyList()

    /*---------------------------------------------------*/

    fun loadTeams(group: String, year: Int, orderBy: RankingOrderBy, reverse: Boolean = false)
    {
        this.orderBy = orderBy
        viewModelScope.launch {
            if(mTeams.isEmpty())
            {
                val teams = TeamManager.retrieveTeamsInSeason(group, year)
                if(teams is ValidTeams)
                {
                    mTeams = teams.self
                    getSortedTeams(teams.self, year, reverse)
                }
            }
            else getSortedTeams(mTeams, year, reverse)
        }
    }

    private suspend fun getSortedTeams(teams: List<Team>, year: Int, reverse: Boolean)
    {
        var sortedTeams: List<Team> = listOf()
        withContext(Default) {
            if(teamByPoints.isEmpty() && (orderBy == RankingOrderBy.POINTS || orderBy == RankingOrderBy.WINS))
            {
                sortedTeams = RankingSolver(teams, year).withOrder(orderBy).sort()
                teamByPoints = sortedTeams
            }
            else if(teamByPoints.isNotEmpty() && (orderBy == RankingOrderBy.POINTS || orderBy == RankingOrderBy.WINS))
            {
                sortedTeams = teamByPoints
            }
            else if(teamByPoints.isEmpty() && orderBy == RankingOrderBy.LOSSES)
            {
                sortedTeams = RankingSolver(teams, year).withOrder(orderBy).sort()
                teamByPoints = sortedTeams.reversed()
            }
            else if(teamByPoints.isNotEmpty() && orderBy == RankingOrderBy.LOSSES)
            {
                sortedTeams = teamByPoints.reversed()
            }
            else
            {
                sortedTeams = RankingSolver(teams, year).withOrder(orderBy).sort()
            }


            if(reverse) sortedTeams = sortedTeams.reversed()
        }
        _teams.value = sortedTeams
    }
}
