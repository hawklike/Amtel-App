package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import kotlinx.coroutines.tasks.await

class TeamDAO : DAO
{
    override suspend fun <T> insert(entity: T)
    {
        if(entity is Team)
        {
            val docReference = Firebase.firestore
                .collection("teams")
                .document()
            entity.id = docReference.id
            docReference.set(entity).await()
        }
        else throw IllegalArgumentException("TeamDAO::insert(): entity is not type of Team and should be")
    }

    override suspend fun find(id: String): DocumentSnapshot
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun update(documentId: String, mapOfFieldsAndValues: Map<String, Any?>): Void?
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}