package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group

class GroupDAO : DAO
{
    private val collection = "groups"

    override suspend fun <T> insert(entity: T)
    {
        if(entity is Group) insert(collection, entity)
        else throw IllegalArgumentException("GroupDAO::insert(): entity is not type of Group and should be")
    }

    override suspend fun find(id: String): DocumentSnapshot = find(collection, id)

    override suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>): Unit
            = update(collection, documentId, mapOfFieldsAndValues)

    override suspend fun delete(documentId: String): Unit = delete(collection, documentId)
}