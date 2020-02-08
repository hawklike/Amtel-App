package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User

class UserDAO : DAO
{
    private val collection = "users"

    override suspend fun <T> insert(entity: T)
    {
        if(entity is User) insert(collection, entity)
        else throw IllegalArgumentException("UserDAO::insert(): entity is not type of User and should be")
    }

    override suspend fun find(id: String): DocumentSnapshot = find(collection, id)

    override suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>): Unit
            = update(collection, documentId, mapOfFieldsAndValues)

    override suspend fun delete(documentId: String): Unit = delete(collection, documentId)

    fun retrieveAll(orderBy: String): Query
    {
        return Firebase.firestore
            .collection(collection)
            .orderBy(orderBy, Query.Direction.ASCENDING)
    }
}