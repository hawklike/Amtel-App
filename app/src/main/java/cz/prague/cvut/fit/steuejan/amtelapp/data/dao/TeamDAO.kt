package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import kotlinx.coroutines.tasks.await

class TeamDAO : DAO
{
    override suspend fun <T> insert(entity: T)
    {
        if(entity is Team)
        {
            val collection = Firebase.firestore.collection("teams")
            val document = entity.id?.let { collection.document(it) } ?: collection.document()
            entity.id = document.id
            document.set(entity, SetOptions.merge()).await()
        }
        else throw IllegalArgumentException("TeamDAO::insert(): entity is not type of Team and should be")
    }

    override suspend fun find(id: String): DocumentSnapshot
    {
        return Firebase.firestore
            .collection("teams")
            .document(id)
            .get()
            .await()
    }

    override suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>): Void?
    {
        return Firebase.firestore
            .collection("teams")
            .document(documentId)
            .update(mapOfFieldsAndValues)
            .await()
    }
}