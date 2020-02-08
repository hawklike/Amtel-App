package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.TeamDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.states.NoTeam
import cz.prague.cvut.fit.steuejan.amtelapp.states.TeamState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TeamManager
{
    suspend fun addTeam(team: Team): Team? = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            TeamDAO().insert(team)
            Log.i(TAG, "addUser(): $team successfully added to database")
            team
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addUser(): $team not added to database because $ex")
            null
        }
    }

    suspend fun updateTeam(documentId: String, mapOfFieldsAndValues: Map<String, Any?>): Boolean = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            TeamDAO().update(documentId, mapOfFieldsAndValues)
            Log.i(TAG, "updateTeam(): team with id $documentId successfully updated with $mapOfFieldsAndValues")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateTeam(): team with id $documentId not updated because $ex")
            false
        }
    }

    suspend fun findTeam(id: String): TeamState = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            val team = TeamDAO().find(id).toObject<Team>()
            Log.i(TAG, "findTeam(): $team found in database")
            team?.let { ValidTeam(team) } ?: NoTeam
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findTeam(): team with $id not found in database because ${ex.message}")
            NoTeam
        }
    }

    fun retrieveAllUsers(teamId: String, orderBy: UserOrderBy = UserOrderBy.SURNAME): Query
    {
        val dao = TeamDAO()
        return when(orderBy)
        {
            UserOrderBy.NAME -> dao.retrieveAllUsers("name", teamId)
            UserOrderBy.SURNAME -> dao.retrieveAllUsers("surname", teamId)
            UserOrderBy.TEAM -> dao.retrieveAllUsers("teamName", teamId)
            UserOrderBy.EMAIL -> dao.retrieveAllUsers("email", teamId)
            UserOrderBy.SEX -> dao.retrieveAllUsers("sex", teamId)
        }
    }

    private const val TAG = "TeamManager"
}