package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
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

    fun signUpUser(context: Context, email: String, password: String, listener: FirebaseUserListener)
    {
        val firebaseOptions = FirebaseOptions.Builder()
            .setDatabaseUrl("https://amtel-app.firebaseio.com")
            .setApiKey("AIzaSyD9jT0sInwya6lZ1nItWV4H-My3ndF5YFc")
            .setApplicationId("amtel-app")
            .build()

        val auth2 = try {
            val myApp = FirebaseApp.initializeApp(context, firebaseOptions, "amtel-helper")
            FirebaseAuth.getInstance(myApp)
        }
        catch (e: IllegalStateException) {
            FirebaseAuth.getInstance(FirebaseApp.getInstance("amtel-helper"))
        }

        auth2.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful)
                {
                    listener.onSignUpCompleted(auth2.currentUser?.uid)
                    auth2.signOut()
                    Log.d(TAG, "successful registration")
                }
                else
                {
                    listener.onSignUpCompleted(null)
                    Log.d(TAG, "registration failed: ${task.exception?.message}")
                }
            }
    }

    fun signInUser(email: String, password: String, listener: FirebaseUserListener)
    {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful)
                {
                    listener.onSignInCompleted(auth.currentUser)
                    Log.d(TAG, "successful login")
                }
                else
                {
                    listener.onSignInCompleted(user = null)
                    Log.d(TAG, "unsuccessful login: ${task.exception?.message}")
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    interface FirebaseUserListener
    {
        fun onSignInCompleted(user: FirebaseUser?) {}
        fun onSignUpCompleted(uid: String?) {}
    }

}