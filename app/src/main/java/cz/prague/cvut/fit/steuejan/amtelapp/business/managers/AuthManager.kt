package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import cz.prague.cvut.fit.steuejan.amtelapp.R

object AuthManager
{
    private const val TAG = "authManager"
    val auth by lazy { FirebaseAuth.getInstance() }

    fun getProfileDrawerOption(context: Context): String =
        auth.currentUser?.let { context.getString(R.string.account) }
            ?: context.getString(R.string.login)

    fun signUpUser(email: String, password: String, listener: FirebaseUserListener)
    {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful)
                {
                    listener.onTaskCompleted(auth.currentUser)
                    Log.d(TAG, "successfully created a new user")
                }
                else
                {
                    listener.onTaskCompleted(user = null)
                    Log.d(TAG, "unsuccessfully created a new user")
                }
            }
    }

    fun signInUser(email: String, password: String, listener: FirebaseUserListener)
    {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful)
                {
                    listener.onTaskCompleted(auth.currentUser)
                    Log.d(TAG, "successful login")
                }
                else
                {
                    listener.onTaskCompleted(user = null)
                    Log.d(TAG, "unsuccessful login: ${task.exception?.message}")
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    interface FirebaseUserListener
    {
        fun onTaskCompleted(user: FirebaseUser?)
    }

}