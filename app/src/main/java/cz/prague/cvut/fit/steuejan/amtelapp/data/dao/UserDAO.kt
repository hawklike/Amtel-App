package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserDAO : DAO
{
    override fun <T> insert(entity: T): Task<Void>
    {
        if(entity is User)
        {
            return Firebase.firestore
                .collection("users")
                .document(entity.id)
                .set(entity)
        }
        else throw IllegalArgumentException("UserDAO::insert(): entity is not type of User and should be")
    }

    override suspend fun find(id: String): DocumentSnapshot = withContext(Dispatchers.IO)
    {
        return@withContext Firebase.firestore
            .collection("users")
            .document(id)
            .get()
            .await()
    }
}