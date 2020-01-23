package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

interface DAO
{
    fun <T> insert(entity: T): Task<Void>
    suspend fun find(id: String): DocumentSnapshot
}
