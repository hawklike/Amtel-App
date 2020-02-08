package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import kotlinx.coroutines.tasks.await

class TeamDAO : DAO
{
    private val collection = "teams"

    override suspend fun <T> insert(entity: T)
    {
        if(entity is Team)
        {
            val collection = Firebase.firestore.collection(collection)
            val document = entity.id?.let { collection.document(it) } ?: collection.document()
            entity.id = document.id
            document.set(entity, SetOptions.merge()).await()
        }
        else throw IllegalArgumentException("TeamDAO::insert(): entity is not type of Team and should be")
    }

    override suspend fun find(id: String): DocumentSnapshot
    {
        return Firebase.firestore
            .collection(collection)
            .document(id)
            .get()
            .await()
    }

    override suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>)
    {
        Firebase.firestore
            .collection(collection)
            .document(documentId)
            .update(mapOfFieldsAndValues)
            .await()
    }

    override suspend fun delete(documentId: String)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun retrieveAll(orderBy: String): Query
    {
        return Firebase.firestore
            .collection(collection)
            .orderBy(orderBy, Query.Direction.ASCENDING)
    }

    fun retrieveAllUsers(orderBy: String, teamId: String): Query
    {
        return Firebase.firestore
            .collection("users")
            .whereEqualTo("teamId", teamId)
            .orderBy(orderBy, Query.Direction.ASCENDING)
    }
}