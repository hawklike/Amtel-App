package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.UserDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole

object UserManager
{
    fun addUser(id: String, name: String, surname: String, email: String, role: UserRole)
    {
        val dao = UserDAO()
        val user = User(id, name, surname, email, UserRole.isTM(role))
        dao.insert(user)
            .addOnSuccessListener { Log.i(TAG, "addUser(): $user successfully added to database") }
            .addOnFailureListener { Log.e(TAG, "addUser(): $user not added to database because $it")}
    }



    //TODO: implement this method [3]
    fun findUser(id: String?)
    {

    }

    private const val TAG = "UserManager"

    //TODO: implement add new user [2]

}