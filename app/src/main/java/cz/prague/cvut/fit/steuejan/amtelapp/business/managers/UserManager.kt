package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SearchPreparation
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.StringUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.UserDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.PlayerRounds
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Rounds
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object UserManager
{
    suspend fun setUser(user: User): User? = withContext(IO)
    {
        val (englishName, englishSurname) = StringUtil.prepareCzechOrdering(user.name, user.surname)
        user.englishName = englishName
        user.englishSurname = englishSurname

        user.searchSurname = SearchPreparation(user.surname).preparedText

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

    suspend fun findUser(id: String?): User? = withContext(IO)
    {
        if(id == null) return@withContext null
        return@withContext try
        {
            val user = UserDAO().findById(id).toObject<User>()
            Log.i(TAG, "findUser(): $user found in database")
            user
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findUser(): user with $id not found in database because ${ex.message}")
            null
        }
    }

    suspend fun <T> findUsers(field: String, value: T?): List<User>? = withContext(IO)
    {
        return@withContext try
        {
            val users = UserDAO().find(field, value).toObjects<User>()
            Log.i(TAG, "findUsers(): $users where $field is $value found successfully")
            users
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findUsers(): documents not found because ${ex.message}")
            null
        }
    }

    suspend fun updateUser(documentId: String?, mapOfFieldsAndValues: Map<String, Any?>): Boolean = withContext(IO)
    {
        if(documentId == null) return@withContext false
        return@withContext try
        {
            UserDAO().update(documentId, mapOfFieldsAndValues)
            Log.i(TAG, "updateUser(): user with id $documentId successfully updated with $mapOfFieldsAndValues")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateUser(): user with id $documentId not updated because ${ex.message}")
            false
        }
    }

    suspend fun deleteUser(userId: String?): Boolean = withContext(IO)
    {
        if(userId == null) return@withContext false
        return@withContext try
        {
            UserDAO().delete(userId)
            Log.i(TAG, "deleteUser(): user with id $userId successfully deleted")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "deleteUser(): user with id $userId not deleted because ${ex.message}")
            false
        }
    }

    fun retrieveAllUsers(): Query
            = UserDAO().retrieveAllUsers()

    fun retrieveUsersByPrefix(textToSearch: String): Query
    {
        val preparation = SearchPreparation(textToSearch)
        return UserDAO().retrieveTeamsByPrefix(preparation.preparedText, searchSurname)
    }

    suspend fun addRound(userId: String, round: Round, roundPosition: Int)
    {
        var playerRounds = UserDAO().getRounds(userId).toObject<PlayerRounds>()

        var rounds: Rounds?
        if(playerRounds == null)
        {
            rounds = Rounds().setRound(round, roundPosition)
        }
        else
        {
            rounds = playerRounds.rounds[round.matchId]
            rounds = rounds?.setRound(round, roundPosition) ?: Rounds().setRound(round, roundPosition)
        }

        playerRounds = PlayerRounds(rounds = mutableMapOf(round.matchId to rounds))
        UserDAO().addMatches(userId, playerRounds)
    }

    private const val TAG = "UserManager"

    const val searchSurname = "searchSurname"
}