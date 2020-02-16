package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Entity
import kotlinx.coroutines.tasks.await

interface DAO
{
    val collection: String
    suspend fun <T> insert(entity: T)
    suspend fun findById(id: String): DocumentSnapshot
    suspend fun <T> find(field: String, value: T?): QuerySnapshot
    suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>)
    suspend fun delete(documentId: String)

    suspend fun insert(collectionName: String, entity: Entity)
    {
        val collection = Firebase.firestore.collection(collectionName)
        val document = entity.id?.let { collection.document(it) } ?: collection.document()
        entity.id = document.id
        document.set(entity, SetOptions.merge()).await()
    }

    suspend fun findById(collectionName: String, id: String): DocumentSnapshot
    {
        return Firebase.firestore
            .collection(collectionName)
            .document(id)
            .get()
            .await()
    }

    suspend fun <T> find(collectionName: String, field: String, value: T?): QuerySnapshot
    {
        return Firebase.firestore
            .collection(collectionName)
            .whereEqualTo(field, value)
            .get()
            .await()
    }


    suspend fun update(collectionName: String, documentId: String, mapOfFieldsAndValues: Map<String, Any?>)
    {
        Firebase.firestore
            .collection(collectionName)
            .document(documentId)
            .update(mapOfFieldsAndValues)
            .await()
    }

    suspend fun delete(collectionName: String, documentId: String)
    {
        Firebase.firestore
            .collection(collectionName)
            .document(documentId)
            .delete()
            .await()
    }

    fun retrieveAll(collectionName: String, orderBy: String): Query
    {
        return Firebase.firestore
            .collection(collectionName)
            .orderBy(orderBy, Query.Direction.ASCENDING)
    }

    suspend fun retrieveAll(collectionName: String): QuerySnapshot
    {
        return Firebase.firestore
            .collection(collectionName)
            .get()
            .await()
    }
}
