package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.content.res.ColorStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowGroupsBossAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import kotlinx.coroutines.launch

class ShowGroupsBossAdapterVM : ViewModel()
{
    fun confirmInput(text: String, rounds: Int): Boolean
    {
        val n: Int

        try { n = text.toInt() }
        catch(ex: NumberFormatException) { return false }

        if(n % 2 == 0 || n < rounds) return false

        return true
    }

    fun handleVisibility(visible: Boolean, group: Group, holder: ShowGroupsBossAdapter.ViewHolder)
    {
        viewModelScope.launch {
            if(GroupManager.updateGroup(group.id, mapOf(GroupManager.visibility to visible)))
            {
                if(visible)
                {
                    holder.visibility.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.blue))
                    toast("Skupina ${group.name} je nyní viditelná.")
                }
                else
                {
                    holder.visibility.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.lightGrey))
                    toast("Skupina ${group.name} je nyní skrytá.")
                }
            }
        }
    }

//    fun generateMatches(group: Group, rounds: Int)
//    {
//        GlobalScope.launch {
//            with(TeamManager.findTeams("groupId", group.id)) {
//                if(this is ValidTeams)
//                {
//                    try
//                    {
//                        createMatches(self, rounds, group)
//                        Log.i("GenerateMatches", "matches successfully generated")
//                        withContext(Main) {
//                            toast(context.getString(R.string.group) + " ${group.name} " + context.getString(R.string.successfully_generated))
//                        }
//                    }
//                    catch(ex: Exception)
//                    {
//                        Log.e("GenerateMatches", "matches not generated generated because ${ex.message}")
//                    }
//                }
//            }
//        }
//    }
//
//    private suspend fun createMatches(teams: List<Team>, rounds: Int, group: Group)
//    {
//        withContext(Default) {
//            val tournament = RobinRoundTournament()
//            tournament.setTeams(teams)
//            tournament.setRounds(rounds)
//            tournament.createMatches(group).forEach {
//                with(MatchManager.setMatch(it)) {
//                    if(this is ValidMatch) addMatchToTeams(self, teams)
//                }
//            }
//        }
//
//        val map = group.rounds
//        map[DateUtil.actualYear] = rounds
//        GroupManager.updateGroup(group.id, mapOf("rounds" to map))
//    }
//
//    private suspend fun addMatchToTeams(match: Match, teams: List<Team>)
//    {
//        val homeTeam = teams.find { it.id == match.homeId }
//        val awayTeam = teams.find { it.id == match.awayId }
//
//        homeTeam?.let { TeamManager.setTeam(it) }
//        awayTeam?.let { TeamManager.setTeam(it) }
//    }
//
//    fun setRank(group: Group, adapterPosition: Int)
//    {
//        viewModelScope.launch {
//            GroupManager.updateGroup(group.id, mapOf("rank" to adapterPosition))
//        }
//    }
//
//    fun deleteGroup(group: Group)
//    {
//        GlobalScope.launch {
//            group.teamIds[DateUtil.actualYear]?.forEach { teamId ->
//                TeamManager.updateTeam(teamId, mapOf("groupName" to null, "groupId" to null))
//            }
//            GroupManager.deleteGroup(group.id)
//        }
//    }
//
//    fun regenerateMatches(group: Group, rounds: Int)
//    {
//        GlobalScope.launch {
//            val year = DateUtil.actualYear
//            var ok = true
//
//            if(MatchManager.deleteAllMatches(group.id, year.toInt()))
//            {
//                group.teamIds[DateUtil.actualYear]?.forEach { teamId ->
//                    if(!clearTeamStatistics(teamId, year)) ok = false
//                }
//                if(ok) generateMatches(group, rounds)
//            }
//        }
//    }
//
//    private suspend fun clearTeamStatistics(teamId: String, year: String): Boolean
//    {
//        val team = TeamManager.findTeam(teamId)
//        if(team is ValidTeam)
//        {
//            team.self.apply {
//                pointsPerMatch[year] = mutableMapOf()
//                pointsPerYear[year] = 0
//                winsPerYear[year] = 0
//                lossesPerYear[year] = 0
//                matchesPerYear[year] = 0
//                setsPositivePerMatch[year] = mutableMapOf()
//                setsNegativePerMatch[year] = mutableMapOf()
//                positiveSetsPerYear[year] = 0
//                negativeSetsPerYear[year] = 0
//            }
//            return TeamManager.setTeam(team.self) != null
//        }
//        return false
//    }
}