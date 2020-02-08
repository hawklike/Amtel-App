package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

interface DAO
{
    suspend fun <T> insert(entity: T)
    suspend fun find(id: String): DocumentSnapshot
    suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>)
    suspend fun delete(documentId: String)
    fun retrieveAll(orderBy: String): Query
}
