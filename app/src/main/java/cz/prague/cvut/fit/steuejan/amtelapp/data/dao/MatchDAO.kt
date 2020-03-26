package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import kotlinx.coroutines.tasks.await

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

    fun getMatches(round: Int, groupId: String, year: Int): Query
    {
        return Firebase.firestore
            .collection(collection)
            .whereEqualTo("round", round)
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("year", year)
            .orderBy("home", Query.Direction.ASCENDING)
    }

    suspend fun getMatches(teamId: String, year: Int): QuerySnapshot
    {
        return Firebase.firestore
            .collection(collection)
            .whereArrayContains("teams", teamId)
            .whereEqualTo("year", year)
            .get()
            .await()
    }

    fun getMatches(teamId: String, orderBy: String = "dateAndTime"): Query
    {
        return Firebase.firestore
            .collection(collection)
            .whereArrayContains("teams", teamId)
            .orderBy(orderBy, Query.Direction.DESCENDING)
    }

    suspend fun getGroupMatches(groupId: String, year: Int): QuerySnapshot
    {
        return Firebase.firestore
            .collection(collection)
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("year", year)
            .get()
            .await()
    }
}