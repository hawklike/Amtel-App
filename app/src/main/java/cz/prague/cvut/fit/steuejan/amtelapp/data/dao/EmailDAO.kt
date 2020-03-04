package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class EmailDAO
{
    suspend fun getPassword(): DocumentSnapshot
    {
        return Firebase.firestore
            .collection("email_password")
            .document("noreply.amtelopava@gmail.com")
            .get()
            .await()
    }
}