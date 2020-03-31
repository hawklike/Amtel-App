package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.MatchDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Results
import cz.prague.cvut.fit.steuejan.amtelapp.states.MatchState
import cz.prague.cvut.fit.steuejan.amtelapp.states.NoMatch
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidMatch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object MatchManager
{
    private const val TAG = "MatchManager"

    suspend fun setMatch(match: Match): MatchState = withContext(IO)
    {
        match.teams = listOf(match.homeId, match.awayId)
        return@withContext try
        {
            MatchDAO().insert(match)
            Log.i(TAG, "addMatch(): $match successfully added to database")
            ValidMatch(match)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addMatch(): $match not added to database because ${ex.message}")
            NoMatch
        }
    }

    suspend fun updateMatch(documentId: String?, mapOfFieldsAndValues: Map<String, Any?>): Boolean = withContext(IO)
    {
        if(documentId == null) return@withContext false
        return@withContext try
        {
            MatchDAO().update(documentId, mapOfFieldsAndValues)
            Log.i(TAG, "updateMatch(): match with id $documentId successfully updated with $mapOfFieldsAndValues")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateMatch(): match with id $documentId not updated because ${ex.message}")
            false
        }
    }

    fun getResults(round: Round): Results
    {
        val sets = round.homeSets?.let { "${round.homeSets} : ${round.awaySets}" } ?: "N/A"

        val tmpGames = "${round.homeGemsSet1}:${round.awayGemsSet1}, ${round.homeGemsSet2}:${round.awayGemsSet2}"
        val games = when(round.homeGemsSet1)
        {
            null -> ""
            else -> {
                when(round.homeGemsSet3)
                {
                    null -> tmpGames
                    else -> "$tmpGames, ${round.homeGemsSet3}:${round.awayGemsSet3}"
                }
            }
        }

        return Results(sets, games)
    }

    fun getMatches(round: Int, group: Group): Query
            = MatchDAO().getMatches(round, group.id!!, DateUtil.actualSeason.toInt())

    suspend fun getCommonMatches(team1: Team, team2: Team, year: Int): List<Match> = withContext(IO)
    {
        return@withContext try
        {
            val matchesTeam1 = MatchDAO().getMatches(team1.id!!, year).toObjects<Match>()
            val matches = matchesTeam1.filter { it.teams.contains(team2.id!!) }
            Log.i(TAG, "getCommonMatches(): $matches for $team1 and $team2 in $year successfully retrieved from database")
            matches
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "getCommonMatches(): matches for $team1 and $team2 in $year not found in database because ${ex.message}")
            listOf<Match>()
        }
    }

    suspend fun deleteAllMatches(groupId: String?, year: Int): Boolean = withContext(IO)
    {
        if(groupId == null) return@withContext false
        return@withContext try
        {
            var ok = true
            val matches = MatchDAO().getGroupMatches(groupId, year).toObjects<Match>()
            matches.forEach { if(!deleteMatch(it.id)) ok = false }
            ok
        }
        catch(ex: Exception) { false }
    }

    suspend fun deleteMatch(matchId: String?): Boolean = withContext(IO)
    {
        if(matchId == null) return@withContext false
        return@withContext try
        {
            MatchDAO().delete(matchId)
            Log.i(TAG, "deleteMatch(): match with id $matchId successfully deleted")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "deleteMatch(): match with id $matchId not deleted because ${ex.message}")
            false
        }
    }
}