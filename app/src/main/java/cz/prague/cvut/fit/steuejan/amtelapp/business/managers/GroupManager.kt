package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.GroupDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object GroupManager
{
    private const val TAG = "GroupManager"

    suspend fun setGroup(group: Group): GroupState = withContext(IO)
    {
        return@withContext try
        {
            GroupDAO().insert(group)
            Log.i(TAG, "addGroup(): $group successfully added to database")
            ValidGroup(group)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addGroup(): $group not added to database because ${ex.message}")
            NoGroup
        }
    }

    suspend fun addPlayoff(playOff: Group): GroupState = withContext(IO)
    {
        return@withContext try
        {
            GroupDAO().insertPlayOff(playOff)
            Log.i(TAG, "addPlayOff(): $playOff successfully added to database")
            ValidGroup(playOff)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addPlayOff(): $playOff not added to database because ${ex.message}")
            NoGroup
        }
    }

    suspend fun findGroup(id: String?): GroupState = withContext(IO)
    {
        if(id == null) return@withContext NoGroup
        return@withContext try
        {
            val group = GroupDAO().findById(id).toObject<Group>()
            Log.i(TAG, "findGroup(): $group found in database")
            group?.let { ValidGroup(group) } ?: NoGroup
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findGroup(): group with $id not found in database because ${ex.message}")
            NoGroup
        }
    }

    suspend fun deleteGroup(groupId: String?): Boolean = withContext(IO)
    {
        if(groupId == null) return@withContext false
        return@withContext try
        {
            GroupDAO().delete(groupId)
            Log.i(TAG, "deleteUser(): group with id $groupId successfully deleted")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "deleteUser(): group with id $groupId not deleted because ${ex.message}")
            false
        }
    }

    suspend fun updateGroup(documentId: String?, mapOfFieldsAndValues: Map<String, Any?>): Boolean = withContext(IO)
    {
        if(documentId == null) return@withContext false
        return@withContext try
        {
            GroupDAO().update(documentId, mapOfFieldsAndValues)
            Log.i(TAG, "updateGroup(): group with id $documentId successfully updated with $mapOfFieldsAndValues")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateGroup(): group with id $documentId not updated because $ex")
            false
        }
    }

    suspend fun <T> findGroups(field: String, value: T?): GroupState = withContext(IO)
    {
        return@withContext try
        {
            val groups = GroupDAO().find(field, value).toObjects<Group>()
            Log.i(TAG, "findGroups(): $groups where $field is $value found successfully")
            ValidGroups(groups)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findGroups(): groups not found because ${ex.message}")
            NoGroup
        }
    }

    suspend fun retrieveAllGroups(): GroupState = withContext(IO)
    {
        return@withContext try
        {
            val groups = GroupDAO().retrieveAll().toObjects<Group>().sortedBy { it.rank }
            Log.i(TAG, "retrieveAll(): $groups retrieved successfully")
            ValidGroups(groups)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveAll(): groups not retrieved because ${ex.message}")
            NoGroup
        }
    }

    suspend fun retrieveAllGroupsExceptPlayoff(orderBy: String = "rank"): GroupState = withContext(IO)
    {
        return@withContext try
        {
            val snapshots = GroupDAO().retrieveAllGroupsExceptPlayoff(orderBy).get().await()
            val groups = snapshots.toObjects<Group>()
            Log.i(TAG, "retrieveAllGroupsExceptPlayOff(): $groups except playoff retrieved successfully")
            ValidGroups(groups)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveAllGroupsExceptPlayOff(): groups not retrieved because ${ex.message}")
            NoGroup
        }
    }

    suspend fun retrieveAllGroupsPlayingPlayoff(orderBy: String = "rank"): GroupState = withContext(IO)
    {
        return@withContext try
        {
            val groups = GroupDAO().retrieveAllGroupsPlayingPlayoff(orderBy).toObjects<Group>()
            Log.i(TAG, "retrieveAllGroupsPlayingPlayoff(): $groups playing playoff retrieved successfully")
            ValidGroups(groups)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveAllGroupsPlayingPlayoff(): groups not retrieved because ${ex.message}")
            NoGroup
        }
    }

    suspend fun retrieveAllGroupsNotPlayingPlayoff(orderBy: String = "rank"): GroupState = withContext(IO)
    {
        return@withContext try
        {
            val groups = GroupDAO().retrieveAllGroupsNotPlayingPlayoff(orderBy).toObjects<Group>()
            Log.i(TAG, "retrieveAllGroupsNotPlayingPlayoff(): $groups not playing playoff retrieved successfully")
            ValidGroups(groups)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveAllGroupsNotPlayingPlayoff(): groups not retrieved because ${ex.message}")
            NoGroup
        }
    }

    suspend fun retrieveTeamsInGroup(groupId: String?): TeamState = withContext(IO)
    {
        if(groupId == null) return@withContext NoTeam
        return@withContext try
        {
            val teams = GroupDAO().retrieveTeamsInGroup(groupId).toObjects<Team>()
            Log.i(TAG, "retrieveTeamsInGroup(): $teams in $groupId retrieved successfully")
            ValidTeams(teams)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveTeamsInGroup(): teams not retrieved because ${ex.message}")
            NoTeam
        }
    }


    fun retrieveAllGroups(orderBy: String): Query
            = GroupDAO().retrieveAllGroups(orderBy)

    const val name = "name"
    const val visibility = "visibility"
}