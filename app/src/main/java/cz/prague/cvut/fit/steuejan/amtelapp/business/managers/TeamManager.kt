package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.MatchDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.TeamDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.TeamOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.states.NoTeam
import cz.prague.cvut.fit.steuejan.amtelapp.states.TeamState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeams
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object TeamManager
{
    suspend fun addTeam(team: Team): Team? = withContext(IO)
    {
        return@withContext try
        {
            TeamDAO().insert(team)
            Log.i(TAG, "addUser(): $team successfully added to database")
            team
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addUser(): $team not added to database because ${ex.message}")
            null
        }
    }

    suspend fun updateTeam(documentId: String?, mapOfFieldsAndValues: Map<String, Any?>): Boolean = withContext(IO)
    {
        if(documentId == null) return@withContext false
        return@withContext try
        {
            TeamDAO().update(documentId, mapOfFieldsAndValues)
            Log.i(TAG, "updateTeam(): team with id $documentId successfully updated with $mapOfFieldsAndValues")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateTeam(): team with id $documentId not updated because ${ex.message}")
            false
        }
    }

    suspend fun findTeam(id: String?): TeamState = withContext(IO)
    {
        if(id == null) return@withContext NoTeam
        return@withContext try
        {
            val team = TeamDAO().findById(id).toObject<Team>()
            Log.i(TAG, "findTeam(): $team found in database")
            team?.let { ValidTeam(team) } ?: NoTeam
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findTeam(): team with $id not found in database because ${ex.message}")
            NoTeam
        }
    }

    suspend fun <T> findTeams(field: String, value: T?): TeamState = withContext(IO)
    {
        return@withContext try
        {
            val teams = TeamDAO().find(field, value).toObjects<Team>()
            Log.i(TAG, "findTeams(): $teams where $field is $value found successfully")
            ValidTeams(teams)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findTeams(): documents not found because ${ex.message}")
            NoTeam
        }
    }

    fun retrieveAllTeams(orderBy: TeamOrderBy = TeamOrderBy.NAME): Query
    {
        var query: Query? = null
        TeamOrderBy.values().forEach {
            if(orderBy == it) query = TeamDAO().retrieveAllTeams(it.toString())
        }
        return query!!
    }

    suspend fun updateUserInTeam(newUser: User): Boolean = withContext(IO)
    {
        return@withContext try
        {
            TeamDAO().updateUser(newUser)
            Log.i(TAG, "updateUserInTeam() $newUser succesfully updated")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateUserInTeam(): user $newUser not updated because ${ex.message}")
            false
        }
    }

    fun retrieveMatches(team: Team): Query = MatchDAO().getMatches(team.id!!)

    fun retrieveAllUsers(teamId: String, orderBy: UserOrderBy = UserOrderBy.SURNAME): Query
    {
        var query: Query? = null
        UserOrderBy.values().forEach {
            if(orderBy == it) query = TeamDAO().retrieveAllUsers(it.toString(), teamId)
        }
        return query!!
    }

    suspend fun retrieveTeamsInSeason(group: String, year: Int): TeamState = withContext(IO)
    {
        return@withContext try
        {
            val teams = TeamDAO().retrieveTeamsInSeason(group, year).toObjects<Team>()
            Log.i(TAG, "retrieveTeamsInSeason(): $teams which contains pair $group, $year found successfully")
            ValidTeams(teams)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveTeamsInSeason(): documents not found because ${ex.message}")
            NoTeam
        }
    }

    private const val TAG = "TeamManager"
}