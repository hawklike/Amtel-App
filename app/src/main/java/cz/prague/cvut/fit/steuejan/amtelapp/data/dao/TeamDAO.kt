package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Entity
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import kotlinx.coroutines.tasks.await

class TeamDAO : DAO
{
    override val collection = "teams"

    override suspend fun <T> insert(entity: T)
    {
        if(entity is Team) insert(collection, entity)
        else throw IllegalArgumentException("TeamDAO::insert(): entity is not type of Team and should be")
    }

    override suspend fun insert(collectionName: String, entity: Entity)
    {
        super.insert(collectionName, entity)
    }

    override suspend fun findById(id: String): DocumentSnapshot
            = findById(collection, id)

    override suspend fun <T> find(field: String, value: T?): QuerySnapshot
            = find(collection, field, value)

    override suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>): Unit
            = update(collection, documentId, mapOfFieldsAndValues)

    override suspend fun delete(documentId: String): Unit
            = delete(collection, documentId)

    fun retrieveAllTeams(orderBy: String): Query
            = retrieveAll(collection, orderBy)

    fun retrieveAllUsers(orderBy: String, teamId: String): Query
    {
        return Firebase.firestore
            .collection("users")
            .whereEqualTo("teamId", teamId)
            .orderBy(orderBy, Query.Direction.ASCENDING)
    }

    suspend fun updateUser(user: User)
    {
        val teams = Firebase.firestore
            .collection(collection)
            .whereArrayContains("usersId", user.id!!)
            .get()
            .await()

        teams.toObjects<Team>().forEach { team ->
            val oldUser = team.users.find { it.id == user.id }
            team.users.remove(oldUser)
            team.users.add(user)
            update(team.id!!, mapOf("users" to team.users))
        }

    }
}