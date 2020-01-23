package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.UserDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserManager
{
    fun addUser(id: String, name: String, surname: String, email: String, role: UserRole)
    {
        val user = User(id, name, surname, email, UserRole.isTM(role))
        UserDAO().insert(user)
            .addOnSuccessListener { Log.i(TAG, "addUser(): $user successfully added to database") }
            .addOnFailureListener { Log.e(TAG, "addUser(): $user not added to database because $it")}
    }

    suspend fun findUser(id: String): User? = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            val user = UserDAO().find(id).toObject<User>()
            Log.i(TAG, "findUser(): $user found in database")
            user
        } catch(ex: Exception)
        {
            Log.e(TAG, "findUser(): user with $id not found in database because ${ex.message}")
            null
        }
    }

    private const val TAG = "UserManager"
}