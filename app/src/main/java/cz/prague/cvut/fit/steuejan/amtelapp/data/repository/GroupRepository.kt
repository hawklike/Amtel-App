package cz.prague.cvut.fit.steuejan.amtelapp.data.repository

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.TestingUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.GroupDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object GroupRepository
{
    private const val TAG = "GroupRepository"

    suspend fun setGroup(group: Group, merge: Boolean = true): GroupState = withContext(IO)
    {
        return@withContext try
        {
            if(merge) GroupDAO().insert(group)
            else GroupDAO().insert(group, false)
            Log.d(TAG, "setGroup(): group $group successfully added/updated to database")
            ValidGroup(group)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "setGroup(): group $group not added/updated to database because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::setGroup(): group $group not added/updated to database because ${ex.message}")
                throwNonFatal(ex)
            }
            NoGroup
        }
    }

    suspend fun setPlayoff(playOff: Group): GroupState = withContext(IO)
    {
        return@withContext try
        {
            GroupDAO().insertPlayOff(playOff)
            Log.d(TAG, "setPlayoff(): playoff $playOff successfully set in database")
            ValidGroup(playOff)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "setPlayoff(): playoff $playOff not set in database because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::setPlayoff(): playoff $playOff not set in database because ${ex.message}")
                throwNonFatal(ex)
            }
            NoGroup
        }
    }

    suspend fun findGroup(id: String?): GroupState = withContext(IO)
    {
        if(id == null) return@withContext NoGroup
        return@withContext try
        {
            val group = GroupDAO().findById(id).toObject<Group>()
            Log.d(TAG, "findGroup(): group $group found in database")
            group?.let { ValidGroup(group) } ?: NoGroup
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findGroup(): group with id $id not found in database because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::findGroup(): group with id $id not found in database because ${ex.message}")
                throwNonFatal(ex)
            }
            NoGroup
        }
    }

    suspend fun deleteGroup(groupId: String?): Boolean = withContext(IO)
    {
        if(groupId == null) return@withContext false
        return@withContext try
        {
            GroupDAO().delete(groupId)
            Log.d(TAG, "deleteGroup(): group with id $groupId successfully deleted")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "deleteGroup(): group with id $groupId not deleted because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::deleteGroup(): group with id $groupId not deleted because ${ex.message}")
                throwNonFatal(ex)
            }
            false
        }
    }

    suspend fun updateGroup(documentId: String?, mapOfFieldsAndValues: Map<String, Any?>): Boolean = withContext(IO)
    {
        if(documentId == null) return@withContext false
        return@withContext try
        {
            GroupDAO().update(documentId, mapOfFieldsAndValues)
            Log.d(TAG, "updateGroup(): group with id $documentId successfully updated with $mapOfFieldsAndValues")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateGroup(): group with id $documentId not updated because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::updateGroup(): group with id $documentId not updated because ${ex.message}")
                throwNonFatal(ex)
            }
            false
        }
    }

    suspend fun <T> findGroups(field: String, value: T?): GroupState = withContext(IO)
    {
        return@withContext try
        {
            val groups = GroupDAO().find(field, value).toObjects<Group>()
            Log.d(TAG, "findGroups(): groups $groups where $field is $value found successfully")
            ValidGroups(groups)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findGroups(): groups where $field is $value not found because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::findGroups(): groups where $field is $value not found because ${ex.message}")
                throwNonFatal(ex)
            }
            NoGroup
        }
    }

    suspend fun retrieveAllGroups(): GroupState = withContext(IO)
    {
        return@withContext try
        {
            val groups = GroupDAO().retrieveAll().toObjects<Group>().sortedBy { it.rank }
            Log.d(TAG, "retrieveAllGroups(): $groups retrieved successfully")
            ValidGroups(groups)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveAllGroups(): groups not retrieved because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::retrieveAllGroups(): groups not retrieved because ${ex.message}")
                throwNonFatal(ex)
            }
            NoGroup
        }
    }

    suspend fun retrieveAllGroupsExceptPlayoff(orderBy: String = "rank"): GroupState = withContext(IO)
    {
        return@withContext try
        {
            val snapshots = GroupDAO().retrieveAllGroupsExceptPlayoff(orderBy).get().await()
            val groups = snapshots.toObjects<Group>()
            Log.d(TAG, "retrieveAllGroupsExceptPlayOff(): groups $groups except playoff retrieved successfully")
            ValidGroups(groups)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveAllGroupsExceptPlayOff(): groups not retrieved because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::retrieveAllGroupsExceptPlayOff(): groups not retrieved because ${ex.message}")
                throwNonFatal(ex)
            }
            NoGroup
        }
    }

    suspend fun retrieveTeamsInGroup(groupId: String?): TeamState = withContext(IO)
    {
        if(groupId == null) return@withContext NoTeam
        return@withContext try
        {
            val teams = GroupDAO().retrieveTeamsInGroup(groupId).toObjects<Team>()
            Log.d(TAG, "retrieveTeamsInGroup(): teams $teams in group with id $groupId retrieved successfully")
            ValidTeams(teams)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveTeamsInGroup(): teams in group with id $groupId not retrieved because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::retrieveTeamsInGroup(): teams in group with id $groupId not retrieved because ${ex.message}")
                throwNonFatal(ex)
            }
            NoTeam
        }
    }

    const val name = "name"
    const val visibility = "visibility"
}