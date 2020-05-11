package cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.RankingSolver
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.RankingOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.RankingOrderBy.POINTS
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

    var orderBy = POINTS
    var reverseOrder = false

    private var mTeams: List<Team> = emptyList()

    /*---------------------------------------------------*/

    fun loadTeams(groupId: String, year: Int, orderBy: RankingOrderBy, reverse: Boolean = false)
    {
        this.orderBy = orderBy
        viewModelScope.launch {
            if(mTeams.isEmpty())
            {
                val teams = TeamRepository.retrieveTeamsInSeason(groupId, year)
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
            //teams not sorted yet by points
            if(teamByPoints.isEmpty() && orderBy == POINTS)
            {
                sortedTeams = RankingSolver(teams, year).withOrder(orderBy).sort()
                teamByPoints = sortedTeams
            }
            //teams already sorted by points
            else if(teamByPoints.isNotEmpty() && orderBy == POINTS) sortedTeams = teamByPoints
            //sort teams by any other order than points
            else sortedTeams = RankingSolver(teams, year).withOrder(orderBy).sort()

            if(reverse) sortedTeams = sortedTeams.reversed()
        }
        //update live data
        _teams.value = sortedTeams
    }

    fun getPosition(team: Team): Int
    {
        return teamByPoints.indexOf(team).let {
            if(it == -1) 0 else it + 1
        }
    }
}
