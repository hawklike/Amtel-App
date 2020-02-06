package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import kotlinx.coroutines.tasks.await

class UserDAO : DAO
{
    override suspend fun <T> insert(entity: T)
    {
        if(entity is User)
        {
            val collection = Firebase.firestore.collection("users")
            val document = entity.id?.let { collection.document(it) } ?: collection.document()
            entity.id = document.id
            document.set(entity).await()
        }
        else throw IllegalArgumentException("UserDAO::insert(): entity is not type of User and should be")
    }

    override suspend fun find(id: String): DocumentSnapshot
    {
        return Firebase.firestore
            .collection("users")
            .document(id)
            .get()
            .await()
    }

    override suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>)
    {
        Firebase.firestore
            .collection("users")
            .document(documentId)
            .update(mapOfFieldsAndValues)
            .await()
    }

    override suspend fun delete(documentId: String)
    {
        Firebase.firestore
            .collection("users")
            .document(documentId)
            .delete()
            .await()
    }
}