package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager

class LoginFragmentVM : ViewModel()
{
    private val email = MutableLiveData<EmailState>()
    fun confirmEmail(): LiveData<EmailState> = email

    /*---------------------------------------------------*/

    private val password = MutableLiveData<PasswordState>()
    fun confirmPassword(): LiveData<PasswordState> = password

    /*---------------------------------------------------*/

    //TODO: return actual User [3]
    private val user = MutableLiveData<FirebaseUser>()
    fun getUser(): LiveData<FirebaseUser> = user

    /*---------------------------------------------------*/

    //TODO: use a findUser(id: String) [3]
    fun loginUser(email: String, password: String)
    {
        if(confirmCredentials(email, password))
            AuthManager.signInUser(email, password, object: AuthManager.FirebaseUserListener
            {
                override fun onTaskCompleted(user: FirebaseUser?)
                {
                    user?.let { this@LoginFragmentVM.user.value = it }
                }
            })
    }

    private fun confirmCredentials(email: String, password: String): Boolean
    {
        var okEmail = true
        var okPassword = true

        if(email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
            this.email.value = EmailState.ValidEmail(email)
        else
            this.email.value = EmailState.InvalidEmail.also { okEmail = false }

        if(password.isNotEmpty())
            this.password.value = PasswordState.ValidPassword(password)
        else
            this.password.value = PasswordState.InvalidPassword.also { okPassword = false }

        return okEmail && okPassword
    }


    sealed class EmailState
    {
        data class ValidEmail(val email: String) : EmailState()
        object InvalidEmail : EmailState()
    }

    sealed class PasswordState
    {
        data class ValidPassword(val password: String) : PasswordState()
        object InvalidPassword : PasswordState()
    }

}