package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.GroupDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GroupManager
{
    private const val TAG = "GroupManager"

    suspend fun addGroup(group: Group): Group? = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            GroupDAO().insert(group)
            Log.i(TAG, "addGroup(): $group successfully added to database")
            group
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addGroup(): $group not added to database because $ex")
            null
        }
    }
}