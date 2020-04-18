package cz.prague.cvut.fit.steuejan.amtelapp.data.repository

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Query.Direction.ASCENDING
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.TestingUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.MessageDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Message
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object MessageRepository
{
    private const val TAG = "MessageRepository"

    suspend fun addMessage(message: Message, matchId: String?, private: Boolean): Message? = withContext(IO)
    {
        if(matchId == null) return@withContext null
        return@withContext try
        {
            if(message.messageText.isEmpty()) return@withContext null
            MessageDAO().insert(message, updateId(matchId, private))
            Log.d(TAG, "addMessage(): message $message successfully added to a database")
            message
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addMessage(): message $message not added to a database because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::addMessage(): message $message not added to a database because ${ex.message}")
                throwNonFatal(ex)
            }
            null
        }
    }

    fun getMessages(matchId: String, private: Boolean, direction: Query.Direction = ASCENDING): Query
            = MessageDAO().getMessages(updateId(matchId, private), direction)

    private fun updateId(matchId: String, private: Boolean): String
    {
        var updated = matchId
        updated +=
            if(private) "_private"
            else "_public"

        return updated
    }
}