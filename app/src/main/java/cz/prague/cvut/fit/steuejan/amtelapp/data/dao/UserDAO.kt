package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.PlayerRounds
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import kotlinx.coroutines.tasks.await

class UserDAO : DAO
{
    override val collection = "users"

    override suspend fun <T> insert(entity: T)
    {
        if(entity is User) insert(collection, entity)
        else throw IllegalArgumentException("UserDAO::insert(): entity is not type of User and should be")
    }

    override suspend fun findById(id: String): DocumentSnapshot
            = findById(collection, id)

    override suspend fun <T> find(field: String, value: T?): QuerySnapshot
            = find(collection, field, value)

    override suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>): Unit
            = update(collection, documentId, mapOfFieldsAndValues)

    override suspend fun delete(documentId: String): Unit
            = delete(collection, documentId)

    fun retrieveAllUsers(): Query
            = retrieveAllAndGetQuery(collection)

    fun retrieveTeamsByPrefix(textToSearch: String, searchField: String): Query
            = retrieveByPrefix(collection, textToSearch, searchField)

    suspend fun getRounds(userId: String): DocumentSnapshot
    {
        return Firebase.firestore
            .collection(collection)
            .document(userId)
            .collection("rounds")
            .document("playerRounds")
            .get()
            .await()
    }

    suspend fun addMatches(userId: String, rounds: PlayerRounds)
    {
        Firebase.firestore
            .collection(collection)
            .document(userId)
            .collection("rounds")
            .document("playerRounds")
            .set(rounds, SetOptions.merge())
            .await()
    }
}