package cz.prague.cvut.fit.steuejan.amtelapp.business

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.*
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.TestingUtil
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AuthManager
{
    private const val TAG = "AuthManager"

    val auth by lazy { FirebaseAuth.getInstance() }

    val profileDrawerOptionMenu: String
        get()
        {
            return auth.currentUser?.let { context.getString(R.string.account) }
                ?: context.getString(R.string.login)
        }

    suspend fun signUpUser(email: String, password: String): String? = withContext(Dispatchers.IO)
    {
        val firebaseOptions = FirebaseOptions.Builder()
            .setDatabaseUrl(context.getString(R.string.firebase_database_url))
            .setApiKey(context.getString(R.string.google_api_key))
            .setApplicationId(context.getString(R.string.project_id))
            .build()

        //in order to sign up a new user without signing in
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
            Log.d(TAG, "signUpUser(): user with $email successfully registered")
            user
        } catch(ex: Exception)
        {
            Log.e(TAG, "signUpUser(): unsuccessful registration because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::signUpUser(): unsuccessful registration because ${ex.message}")
                throwNonFatal(ex)
            }
            null
        }
    }

    suspend fun signInUser(email: String, password: String): FirebaseUser? = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            val user = auth.signInWithEmailAndPassword(email, password).await().user
            Log.d(TAG, "signInUser(): $user successfully logged in")
            user
        } catch(ex: Exception)
        {
            Log.e(TAG, "signInUser(): unsuccessful login because ${ex.message}")
            null
        }
    }

    suspend fun changePassword(newPassword: String): PasswordState = withContext(Dispatchers.IO)
    {
        val user = currentUser
        return@withContext if(user != null)
        {
            try
            {
                user.updatePassword(newPassword).await()
                Log.d(TAG, "changePassword(): new password successfully changed")
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
            with(TestingUtil) {
                log("$TAG::changePassword(): new password failed because there is no signed user")
                throwNonFatal(RuntimeException("Request for password change when not allowed."))
            }
            InvalidPassword()
        }
    }

    suspend fun changeEmail(newEmail: String): EmailState = withContext(Dispatchers.IO)
    {
        val user = currentUser
        return@withContext if(user != null)
        {
            try
            {
                user.updateEmail(newEmail).await()
                Log.d(TAG, "changeEmail(): new email successfully changed")
                ValidEmail(newEmail)
            }
            catch(ex: Exception)
            {
                Log.e(TAG, "changeEmail(): new email failed because ${ex.message}")
                when(ex)
                {
                    is FirebaseAuthInvalidCredentialsException -> InvalidEmail("Byl zadán nesprávný formát.")
                    is FirebaseAuthUserCollisionException -> InvalidEmail("Zadaný email již existuje.")
                    is FirebaseAuthRecentLoginRequiredException -> InvalidEmail("Uběhla již nějaká doba od posledního přihlášení. Prosím odhlašte se a zkuste to znovu.")
                    is FirebaseAuthInvalidUserException -> InvalidEmail("Váš účet nebyl nalezen (byl smazán nebo deaktivován).")
                    else -> InvalidEmail("Nastala neočekávaná chyba.")
                }
            }
        }
        else
        {
            Log.e(TAG, "changeEmail(): new email failed because there is no signed user")
            InvalidEmail("Váš účet nebyl nalezen. Tuto zprávu byste neměl/a vidět.")
        }
    }

    fun sendResetPassword(email: String)
    {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) toast(R.string.reset_email_sent)
                else toast(R.string.reset_email_failure)
            }
    }

    //if known IDs of home and away team managers, this method retrieves who is currently signed in
    fun getCurrentRole(homeManagerId: String?, awayManagerId: String?): SignedIn
    {
        return currentUser?.let {
            when(it.uid)
            {
                homeManagerId -> SignedIn.HOME_MANAGER
                awayManagerId -> SignedIn.AWAY_MANAGER
                else -> SignedIn.HEAD_OF_LEAGUE
            }
        } ?: SignedIn.NONE
    }

    enum class SignedIn
    {
        HOME_MANAGER, AWAY_MANAGER, HEAD_OF_LEAGUE, NONE
    }

    val currentUser: FirebaseUser?
    get() { return auth.currentUser }
}