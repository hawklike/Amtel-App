package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch

class LoginFragmentVM : ViewModel()
{
    private val email = MutableLiveData<EmailState>()
    fun confirmEmail(): LiveData<EmailState> = email

    /*---------------------------------------------------*/

    private val password = MutableLiveData<PasswordState>()
    fun confirmPassword(): LiveData<PasswordState> = password

    /*---------------------------------------------------*/

    private val user = SingleLiveEvent<UserState>()
    fun getUser(): LiveData<UserState> = user

    /*---------------------------------------------------*/

    fun loginUser(email: String, password: String)
    {
        if(confirmCredentials(email, password))
            AuthManager.signInUser(email, password, object: AuthManager.FirebaseAuthUserListener
            {
                override fun onSignInCompleted(firebaseUser: FirebaseUser?)
                {
                    if(firebaseUser != null)
                    {
                        viewModelScope.launch {
                            val user =  UserManager.findUser(firebaseUser.uid)
                            this@LoginFragmentVM.user.value = user?.let { SignedUser(it) } ?: NoUser
                        }
                    }
                }
            })
    }

    private fun confirmCredentials(email: String, password: String): Boolean
    {
        var okEmail = true
        var okPassword = true

        if(email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
            this.email.value = ValidEmail(email)
        else
            this.email.value = InvalidEmail.also { okEmail = false }

        if(password.isNotEmpty())
            this.password.value = ValidPassword
        else
            this.password.value = InvalidPassword.also { okPassword = false }

        return okEmail && okPassword
    }

}