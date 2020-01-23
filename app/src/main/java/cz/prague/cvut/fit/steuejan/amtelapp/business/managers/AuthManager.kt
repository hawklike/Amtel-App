package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidPassword
import cz.prague.cvut.fit.steuejan.amtelapp.states.PasswordState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


object AuthManager
{
    private const val TAG = "AuthManager"

    val auth by lazy { FirebaseAuth.getInstance() }

    fun getProfileDrawerOption(context: Context): String =
        auth.currentUser?.let { context.getString(R.string.account) }
            ?: context.getString(R.string.login)

    suspend fun signUpUser(context: Context, email: String, password: String): String? = withContext(Dispatchers.IO)
    {
        val firebaseOptions = FirebaseOptions.Builder()
            .setDatabaseUrl("https://amtel-app.firebaseio.com")
            .setApiKey("AIzaSyD9jT0sInwya6lZ1nItWV4H-My3ndF5YFc")
            .setApplicationId("amtel-app")
            .build()

        val auth2 = try
        {
            val myApp = FirebaseApp.initializeApp(context, firebaseOptions, "amtel-helper")
            FirebaseAuth.getInstance(myApp)
        }
        catch (e: IllegalStateException)
        {
            FirebaseAuth.getInstance(FirebaseApp.getInstance("amtel-helper"))
        }

        return@withContext try
        {
            auth2.createUserWithEmailAndPassword(email, password).await()
            val user = auth2.currentUser?.uid
            auth2.signOut()
            Log.i(TAG, "signUpUser(): user with $email and $password successfully registered")
            user
        } catch(ex: Exception)
        {
            Log.e(TAG, "signUpUser(): unsuccessful login because ${ex.message}")
            null
        }
    }

    suspend fun signInUser(email: String, password: String): FirebaseUser? = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            val user = auth.signInWithEmailAndPassword(email, password).await().user
            Log.i(TAG, "signInUser(): $user successfully logged in")
            user
        } catch(ex: Exception)
        {
            Log.e(TAG, "signInUser(): unsuccessful login because ${ex.message}")
            null
        }
    }

    suspend fun changePassword(newPassword: String): PasswordState = withContext(Dispatchers.IO)
    {
        val user = getCurrentUser()
        return@withContext if(user != null)
        {
            try
            {
                user.updatePassword(newPassword).await()
                Log.i(TAG, "changePassword(): new password successfully changed")
                ValidPassword(newPassword)
            }
            catch(ex: Exception)
            {
                Log.e(TAG, "changePassword(): new password failed because ${ex.message}")
                InvalidPassword()
            }
        }
        else
        {
            Log.e(TAG, "changePassword(): new password failed because there is no signed user")
            InvalidPassword()
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}