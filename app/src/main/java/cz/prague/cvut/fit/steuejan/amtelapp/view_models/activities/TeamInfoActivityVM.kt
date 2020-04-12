package cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

typealias Entries = List<PieEntry>
typealias TripleEntries = Triple<Entries, Entries, Entries>

class TeamInfoActivityVM : ViewModel()
{
    var seasonRanking: List<Team> = listOf()
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
                        listOf(PieEntry(winsThisYear.toFloat(), "Letošní výhraná utkání"), PieEntry(lossesThisYear.toFloat(), "Letošní prohraná utkání"))
                    else
                        listOf(PieEntry(1f, "Tým v aktuální sezóně ještě neodehrál žádné utkání."))

                val winsTotal = team.winsPerYear.values.sum()
                val lossesTotal = team.lossesPerYear.values.sum()
                val totalEntries =
                    if(winsTotal + lossesThisYear != 0)
                        listOf(PieEntry(winsTotal.toFloat(), "Výhraná utkání"), PieEntry(lossesTotal.toFloat(), "Prohraná utkání"))
                    else
                        listOf(PieEntry(1f, "Tým ještě neodehrál žádné utkání."))


                val positiveSets = team.positiveSetsPerYear.values.sum()
                val negativeSets = team.negativeSetsPerYear.values.sum()
                val setsEntries =
                    if(positiveSets + negativeSets != 0)
                        listOf(PieEntry(positiveSets.toFloat(), "Celkově získané sety"), PieEntry(negativeSets.toFloat(), "Celkově ztracené sety"))
                    else
                        listOf(PieEntry(1f, "Tým ještě neodehrál žádné sety."))


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

    fun calculateTeamRank()
    {
        viewModelScope.launch {
            for(i in seasonRanking.indices)
            {
                if(seasonRanking[i].id == mTeam?.id)
                {
                    _teamRank.value = i + 1
                    return@launch
                }
            }

            if(seasonRanking.isEmpty()) _teamRank.value = 0
        }
    }

    fun getTeam(id: String)
    {
        viewModelScope.launch {
            val team = TeamManager.findTeam(id)
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
