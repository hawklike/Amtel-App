package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team

class TeamDAO : DAO
{
    private val collection = "teams"

    override suspend fun <T> insert(entity: T)
    {
        if(entity is Team) insert(collection, entity)
        else throw IllegalArgumentException("TeamDAO::insert(): entity is not type of Team and should be")
    }

    override suspend fun find(id: String): DocumentSnapshot = find(collection, id)

    override suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>): Unit
            = update(collection, documentId, mapOfFieldsAndValues)

    override suspend fun delete(documentId: String): Unit = delete(collection, documentId)

    fun retrieveAllUsers(orderBy: String, teamId: String): Query
    {
        return Firebase.firestore
            .collection("users")
            .whereEqualTo("teamId", teamId)
            .orderBy(orderBy, Query.Direction.ASCENDING)
    }
}