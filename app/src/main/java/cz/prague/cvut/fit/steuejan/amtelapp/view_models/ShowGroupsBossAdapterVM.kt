package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.RobinRoundTournament
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidMatch
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeams
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowGroupsBossAdapterVM : ViewModel()
{
    private val _matchesGenerated = SingleLiveEvent<Boolean>()
    val matchesGenerated: LiveData<Boolean> = _matchesGenerated

    /*---------------------------------------------------*/

    fun confirmInput(text: String, rounds: Int): Boolean
    {
        val n: Int

        try { n = text.toInt() }
        catch(ex: NumberFormatException) { return false }

        if(n % 2 == 0 || n < rounds) return false

        return true
    }

    fun generateMatches(group: Group, rounds: Int)
    {
        GlobalScope.launch {
            with(TeamManager.findTeams("groupId", group.id)) {
                if(this is ValidTeams)
                {
                    try
                    {
                        createMatches(self, rounds, group)
                        Log.i("GenerateMatches", "matches successfully generated")
                        withContext(Main) {
                            toast(context.getString(R.string.group) + " ${group.name} " + context.getString(R.string.successfully_generated))
                        }
                    }
                    catch(ex: Exception)
                    {
                        Log.e("GenerateMatches", "matches not generated generated because ${ex.message}")
                    }
                }
            }
        }
    }

    private suspend fun createMatches(teams: List<Team>, rounds: Int, group: Group)
    {
        withContext(Default) {
            val tournament = RobinRoundTournament()
            tournament.setTeams(teams)
            tournament.setRounds(rounds)
            tournament.createMatches(group).forEach {
                with(MatchManager.addMatch(it)) {
                    if(this is ValidMatch) addMatchToTeams(self, teams, group)
                }
            }
        }

        val map = group.rounds
        map[DateUtil.actualYear] = rounds
        GroupManager.updateGroup(group.id, mapOf("rounds" to map))
    }

    private suspend fun addMatchToTeams(match: Match, teams: List<Team>, group: Group)
    {
        val homeTeam = teams.find { it.id == match.homeId }
        val awayTeam = teams.find { it.id == match.awayId }

//        val homeSeasons = homeTeam?.seasons?.toMutableSet()
//        homeSeasons?.add(mapOf(DateUtil.actualYear to group.id!!))
//        homeSeasons?.let { homeTeam.seasons = it.toList() }
//
//        val awaySeasons = awayTeam?.seasons?.toMutableSet()
//        awaySeasons?.add(mapOf(DateUtil.actualYear to group.id!!))
//        awaySeasons?.let { awayTeam.seasons = it.toList() }

        homeTeam?.let { TeamManager.addTeam(it) }
        awayTeam?.let { TeamManager.addTeam(it) }
    }

    fun setRank(group: Group, adapterPosition: Int)
    {
        viewModelScope.launch {
            GroupManager.updateGroup(group.id, mapOf("rank" to adapterPosition))
        }
    }

    fun deleteTeam(group: Group)
    {
        GlobalScope.launch {
            group.teamIds[DateUtil.actualYear]?.forEach { teamId ->
                TeamManager.updateTeam(teamId, mapOf("groupName" to null, "groupId" to null))

            }
            GroupManager.deleteGroup(group.id)
        }
    }
}