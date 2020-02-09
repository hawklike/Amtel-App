package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group

class GroupDAO : DAO
{
    private val collection = "groups"

    override suspend fun <T> insert(entity: T)
    {
        if(entity is Group)
        {
            Firebase.firestore
                .collection(collection)
                .document(entity.name)
                .set(entity)
        }
        else throw IllegalArgumentException("GroupDAO::insert(): entity is not type of Group and should be")
    }

    override suspend fun findById(id: String): DocumentSnapshot
            = findById(collection, id)

    override suspend fun <T> find(field: String, value: T?): QuerySnapshot
            = find(collection, field, value)

    override suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>): Unit
            = update(collection, documentId, mapOfFieldsAndValues)

    override suspend fun delete(documentId: String): Unit
            = delete(collection, documentId)

    suspend fun retrieveAll(): QuerySnapshot
            = retrieveAll(collection)
}