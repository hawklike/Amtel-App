package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.GroupDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.states.GroupState
import cz.prague.cvut.fit.steuejan.amtelapp.states.NoGroup
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroup
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroups
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object GroupManager
{
    private const val TAG = "GroupManager"

    suspend fun addGroup(group: Group): GroupState = withContext(IO)
    {
        return@withContext try
        {
            GroupDAO().insert(group)
            Log.i(TAG, "addGroup(): $group successfully added to database")
            ValidGroup(group)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addGroup(): $group not added to database because $ex")
            NoGroup
        }
    }

    suspend fun findGroup(id: String): GroupState = withContext(IO)
    {
        return@withContext try
        {
            val team = GroupDAO().findById(id).toObject<Group>()
            Log.i(TAG, "findGroup(): $team found in database")
            team?.let { ValidGroup(team) } ?: NoGroup
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findGroup(): team with $id not found in database because ${ex.message}")
            NoGroup
        }
    }

    suspend fun <T> findGroup(field: String, value: T?): GroupState = withContext(IO)
    {
        return@withContext try
        {
            val querySnapshot = GroupDAO().find(field, value)
            val documents = querySnapshot.toObjects<Group>()
            Log.i(TAG, "findGroup(): $documents where $field is $value found successfully\"")
            ValidGroups(documents)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findGroup(): documents not found because $ex")
            NoGroup
        }
    }

    suspend fun retrieveAllGroups(): GroupState = withContext(IO)
    {
        return@withContext try
        {
            val documentSnapshot = GroupDAO().retrieveAll()
            val documents = documentSnapshot.toObjects<Group>()
            Log.i(TAG, "retrieveAll(): $documents retrieved successfully")
            ValidGroups(documents)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveAll(): documents not retrieved because $ex")
            NoGroup
        }
    }

    fun retrieveAllGroups(orderBy: String): Query
            = GroupDAO().retrieveAllGroups(orderBy)
}