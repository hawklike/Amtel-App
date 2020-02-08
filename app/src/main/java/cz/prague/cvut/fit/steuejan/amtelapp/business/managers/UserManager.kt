package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.UserDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Sex
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

object UserManager
{
    suspend fun addUser(name: String, surname: String, email: String, role: UserRole, id: String? = null,
                        sex: Sex = Sex.MAN, birthdate: Date? = null, teamId: String? = null, teamName: String? = null): User? = withContext(Dispatchers.IO)
    {
        val user = User(
            id,
            name,
            surname,
            email,
            birthdate = birthdate,
            sex = Sex.toBoolean(sex),
            role = UserRole.toString(role),
            teamId = teamId,
            teamName = teamName)

        return@withContext try
        {
            UserDAO().insert(user)
            Log.i(TAG, "addUser(): $user successfully added to database")
            user
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addUser(): $user not added to database because $ex")
            null
        }
    }

    suspend fun findUser(id: String): User? = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            val user = UserDAO().find(id).toObject<User>()
            Log.i(TAG, "findUser(): $user found in database")
            user
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findUser(): user with $id not found in database because ${ex.message}")
            null
        }
    }

    suspend fun updateUser(documentId: String, mapOfFieldsAndValues: Map<String, Any?>): Boolean = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            UserDAO().update(documentId, mapOfFieldsAndValues)
            Log.i(TAG, "updateUser(): user with id $documentId successfully updated with $mapOfFieldsAndValues")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateUser(): user with id $documentId not updated because $ex")
            false
        }
    }

    suspend fun findUsers(usersId: List<String>): List<User> = withContext(Dispatchers.IO)
    {
        return@withContext usersId.fold(mutableListOf<User>(), { acc, userId ->
            findUser(userId)?.let { acc.add(it) }
            return@fold acc
        })
    }

    suspend fun deleteUser(userId: String): Boolean = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            UserDAO().delete(userId)
            Log.i(TAG, "deleteUser(): user with id $userId successfully deleted with")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "deleteUser(): user with id $userId not deleted because $ex")
            false
        }
    }

    fun retrieveAllUsers(orderBy: UserOrderBy = UserOrderBy.SURNAME): Query
    {
        val dao = UserDAO()
        return when(orderBy)
        {
            UserOrderBy.NAME -> dao.retrieveAll("name")
            UserOrderBy.SURNAME -> dao.retrieveAll("surname")
            UserOrderBy.TEAM -> dao.retrieveAll("teamName")
            UserOrderBy.EMAIL -> dao.retrieveAll("email")
            UserOrderBy.SEX -> dao.retrieveAll("sex")
        }
    }

    private const val TAG = "UserManager"
}