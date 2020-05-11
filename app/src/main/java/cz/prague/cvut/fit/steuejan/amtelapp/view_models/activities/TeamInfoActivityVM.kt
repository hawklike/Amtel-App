package cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

typealias Entries = List<PieEntry>
typealias TripleEntries = Triple<Entries, Entries, Entries>

class TeamInfoActivityVM : ViewModel()
{
    var mTeam: Team? = null

    /*---------------------------------------------------*/

    private val _team = SingleLiveEvent<Team>()
    val team: LiveData<Team> = _team

    /*---------------------------------------------------*/

    private val _charts = MutableLiveData<TripleEntries>()
    val charts: LiveData<TripleEntries> = _charts

    /*---------------------------------------------------*/

    private val _successRate = MutableLiveData<Int>()
    val successRate: LiveData<Int> = _successRate

    /*---------------------------------------------------*/

    private val _teamRank = MutableLiveData<Int>()
    val teamRank: LiveData<Int> = _teamRank

    fun setTeamRank(rank: Int)
    {
        _teamRank.value = rank
    }

    /*---------------------------------------------------*/

    private val _avgRank = MutableLiveData<Int>()
    val avgRank: LiveData<Int> = _avgRank

    /*---------------------------------------------------*/

    private val _titles = MutableLiveData<Int>()
    val titles: LiveData<Int> = _titles

    /*---------------------------------------------------*/

    private val _matches = MutableLiveData<Int>()
    val matches: LiveData<Int> = _matches

    /*---------------------------------------------------*/

    private val _season = SingleLiveEvent<Int>()
    val season: LiveData<Int> = _season

    /*---------------------------------------------------*/

    fun getChartsData()
    {
        viewModelScope.launch {
            mTeam?.let { team ->
                val winsThisYear = team.winsPerYear[DateUtil.actualSeason] ?: 0
                val lossesThisYear = team.lossesPerYear[DateUtil.actualSeason] ?: 0
                val thisYearEntries =
                    if(winsThisYear + lossesThisYear != 0)
                        listOf(PieEntry(winsThisYear.toFloat(), context.getString(R.string.win_matches_this_year)), PieEntry(lossesThisYear.toFloat(), context.getString(
                                                    R.string.lost_matches_this_year)))
                    else
                        listOf(PieEntry(1f, context.getString(R.string.not_played_matches_this_year)))

                val winsTotal = team.winsPerYear.values.sum()
                val lossesTotal = team.lossesPerYear.values.sum()
                val totalEntries =
                    if(winsTotal + lossesThisYear != 0)
                        listOf(PieEntry(winsTotal.toFloat(), context.getString(R.string.total_win_matches)), PieEntry(lossesTotal.toFloat(), context.getString(
                                                    R.string.total_lost_matches)))
                    else
                        listOf(PieEntry(1f, context.getString(R.string.not_played_any_matches)))


                val positiveSets = team.positiveSetsPerYear.values.sum()
                val negativeSets = team.negativeSetsPerYear.values.sum()
                val setsEntries =
                    if(positiveSets + negativeSets != 0)
                        listOf(PieEntry(positiveSets.toFloat(), context.getString(R.string.total_positive_sets)), PieEntry(negativeSets.toFloat(), context.getString(
                                                    R.string.total_lost_sets)))
                    else
                        listOf(PieEntry(1f, context.getString(R.string.not_played_any_sets)))


                _charts.value = Triple(thisYearEntries, totalEntries, setsEntries)
                _matches.value = team.matchesPerYear.values.sum()

                countSuccessRate(winsTotal, lossesTotal)
            }
        }
    }

    private fun countSuccessRate(winsTotal: Int, lossesTotal: Int)
    {
        val rate = winsTotal.toDouble() / (winsTotal + lossesTotal) * 100
        _successRate.value = if(!rate.isNaN()) rate.roundToInt() else 0
    }

    fun calculateTeamRank(rankedTeams: List<Team>)
    {
        viewModelScope.launch {
            for(i in rankedTeams.indices)
            {
                if(rankedTeams[i].id == mTeam?.id)
                {
                    _teamRank.value = i + 1
                    return@launch
                }
            }

            if(rankedTeams.isEmpty()) _teamRank.value = 0
        }
    }

    fun getTeam(id: String)
    {
        viewModelScope.launch {
            val team = TeamRepository.findTeam(id)
            if(team is ValidTeam) _team.value = team.self
        }
    }

    fun getAverageRank()
    {
        mTeam?.let { team ->
            val avg = team.results.average().let {
                if(!it.isNaN()) it.roundToInt()
                else 0
            }

            val titles = team.results.fold(0) { acc, rank ->
                if(rank == 1) acc + 1
                else acc
            }

            _avgRank.value = avg
            _titles.value = titles
        }
    }
}
