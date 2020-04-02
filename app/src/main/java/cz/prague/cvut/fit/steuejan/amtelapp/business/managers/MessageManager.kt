package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import com.google.firebase.firestore.Query
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.MessageDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Message
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object MessageManager
{
    suspend fun addMessage(message: Message, matchId: String?, private: Boolean): Message? = withContext(IO)
    {
        if(matchId == null) return@withContext null
        return@withContext try
        {
            if(message.messageText.isEmpty()) return@withContext null
            MessageDAO().insert(message, updateId(matchId, private))
            Log.i(TAG, "$message successfully added to a database")
            message
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "$message not added to a database because ${ex.message}")
            null
        }
    }

    fun getMessages(matchId: String, private: Boolean): Query
            = MessageDAO().getMessages(updateId(matchId, private))

    private fun updateId(matchId: String, private: Boolean): String
    {
        var updated = matchId
        updated +=
            if(private) "_private"
            else "_public"

        return updated
    }

    const val TAG = "MessageManager"
}