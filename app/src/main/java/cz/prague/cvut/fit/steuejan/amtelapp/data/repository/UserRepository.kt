package cz.prague.cvut.fit.steuejan.amtelapp.data.repository

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SearchPreparation
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.StringUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.TestingUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.UserDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.PlayerRounds
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Rounds
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object UserRepository
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
            Log.d(TAG, "setUser(): user $user successfully set/updated to database")
            user
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "setUser(): user $user not set/updated to database because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::setUser(): user $user not set/updated to database because ${ex.message}")
                throwNonFatal(ex)
            }
            null
        }
    }

    suspend fun findUser(id: String?): User? = withContext(IO)
    {
        if(id == null) return@withContext null
        return@withContext try
        {
            val user = UserDAO().findById(id).toObject<User>()
            Log.d(TAG, "findUser(): user $user found in database")
            user
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findUser(): user with id $id not found in database because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::findUser(): user with id $id not found in database because ${ex.message}")
                throwNonFatal(ex)
            }
            null
        }
    }

    suspend fun <T> findUsers(field: String, value: T?): List<User>? = withContext(IO)
    {
        return@withContext try
        {
            val users = UserDAO().find(field, value).toObjects<User>()
            Log.d(TAG, "findUsers(): users $users where $field is $value found successfully")
            users
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findUsers(): users where $field is $value not found because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::findUsers(): users where $field is $value not found because ${ex.message}")
                throwNonFatal(ex)
            }
            null
        }
    }

    suspend fun updateUser(documentId: String?, mapOfFieldsAndValues: Map<String, Any?>): Boolean = withContext(IO)
    {
        if(documentId == null) return@withContext false
        return@withContext try
        {
            UserDAO().update(documentId, mapOfFieldsAndValues)
            Log.d(TAG, "updateUser(): user with id $documentId successfully updated with $mapOfFieldsAndValues")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateUser(): user with id $documentId not updated with $mapOfFieldsAndValues because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::updateUser(): user with id $documentId not updated with $mapOfFieldsAndValues because ${ex.message}")
                throwNonFatal(ex)
            }
            false
        }
    }

    suspend fun deleteUser(userId: String?): Boolean = withContext(IO)
    {
        if(userId == null) return@withContext false
        return@withContext try
        {
            UserDAO().delete(userId)
            Log.d(TAG, "deleteUser(): user with id $userId successfully deleted")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "deleteUser(): user with id $userId not deleted because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::setUser(): deleteUser(): user with id $userId not deleted because ${ex.message}")
                throwNonFatal(ex)
            }
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

    suspend fun addRound(userId: String, matchId: String, round: Round?, roundPosition: Int): Boolean = withContext(IO)
    {
        return@withContext try
        {
            var playerRounds = UserDAO().getRounds(userId).toObject<PlayerRounds>()

            var rounds: Rounds?
            if(playerRounds == null)
            {
                rounds = Rounds().setRound(round, roundPosition)
            }
            else
            {
                rounds = playerRounds.rounds[matchId]
                rounds = rounds?.setRound(round, roundPosition) ?: Rounds().setRound(round, roundPosition)
            }

            playerRounds = PlayerRounds(rounds = mutableMapOf(matchId to rounds))
            UserDAO().addMatches(userId, playerRounds)
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addRound(): round $round not added to user with id $userId because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::addRound(): round $round not added to user with id $userId because ${ex.message}")
                throwNonFatal(ex)
            }
            false
        }
    }

    suspend fun deleteRound(userId: String, matchId: String, roundPosition: Int): Boolean
            = withContext(IO) { addRound(userId, matchId, null, roundPosition) }

    suspend fun retrieveRounds(userId: String): PlayerRounds? = withContext(IO)
    {
        return@withContext try
        {
            UserDAO().getRounds(userId).toObject<PlayerRounds>()
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveRounds(): rounds of a user with id $userId not retrieved because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::retrieveRounds(): rounds of a user with id $userId not retrieved because ${ex.message}")
                throwNonFatal(ex)
            }
            null
        }
    }

    private const val TAG = "UserRepository"

    const val searchSurname = "searchSurname"
}