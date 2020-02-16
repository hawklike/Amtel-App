package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match

class MatchDAO : DAO
{
    override val collection: String = "matches"

    override suspend fun <T> insert(entity: T)
    {
        if(entity is Match) insert(collection, entity)
        else throw IllegalArgumentException("MatchDAO::insert(): entity is not type of Match and should be")
    }

    override suspend fun findById(id: String): DocumentSnapshot
            = findById(collection, id)

    override suspend fun <T> find(field: String, value: T?): QuerySnapshot
            = find(collection, field, value)

    override suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>): Unit
            = update(collection, documentId, mapOfFieldsAndValues)

    override suspend fun delete(documentId: String): Unit
            = delete(collection, documentId)
}