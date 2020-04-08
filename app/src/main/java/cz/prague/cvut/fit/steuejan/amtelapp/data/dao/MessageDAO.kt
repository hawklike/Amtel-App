package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Query.Direction.ASCENDING
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Message
import kotlinx.coroutines.tasks.await

class MessageDAO
{
    private val collection = "messages"
    private val subcollection = "matchMessages"

    suspend fun insert(message: Message, matchId: String)
    {
        Firebase.firestore
            .collection(collection)
            .document(matchId)
            .collection(subcollection)
            .add(message)
            .await()
    }

    fun getMessages(matchId: String, direction: Query.Direction): Query
    {
        return Firebase.firestore
            .collection(collection)
            .document(matchId)
            .collection(subcollection)
            .orderBy("sentAt", direction)
    }

    fun getMessagesReference(id: String): DocumentReference
    {
        return Firebase.firestore
            .collection(collection)
            .document(id)

    }
}