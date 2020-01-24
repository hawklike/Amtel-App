package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
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
        {
            viewModelScope.launch {
                val firebaseUser = AuthManager.signInUser(email, password)
                if(firebaseUser != null)
                {
                    val user = UserManager.findUser(firebaseUser.uid)
                    this@LoginFragmentVM.user.value = user?.let { SignedUser(it, user.firstSign) } ?: NoUser
                    user?.let {
                        if(user.firstSign)
                            UserManager.updateUser(user.id, field = "firstSign", newValue = false)
                    }
                }
                else this@LoginFragmentVM.user.value = NoUser
            }
        }
    }

    fun createAfterDialog(user: UserState): Pair<String, String?>
    {
        val title: String
        val message: String?

        if(user is SignedUser)
        {
            title = "Přihlášení proběhlo úspěšně"
            message = when
            {
                UserRole.toRole(user.self.role) != UserRole.TEAM_MANAGER -> null
                user.firstSign -> "Vítejte!\nZdá se, že jste se přihlásil/a poprvé. Jako první prosím vyplňte Vaše osobní údaje v sekci: Účet > OSOBNÍ."
                else -> null
            }
            Log.i(TAG, "getUser(): login was successful - current user: $user")
        }
        else
        {
            title = "Přihlášení se nezdařilo"
            message = "Zadali jste neplatnou kombinaci emailu a hesla."
            Log.e(TAG, "getUser(): login not successful")
        }

        return Pair(title, message)
    }

    private fun confirmCredentials(email: String, password: String): Boolean
    {
        var okEmail = true
        var okPassword = true

        if(email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
            this.email.value = ValidEmail(email)
        else
            this.email.value = InvalidEmail(errorMessage = "Zadejte prosím validní email.").also { okEmail = false }

        if(password.isNotEmpty())
            this.password.value = ValidPassword(password)
        else
            this.password.value = InvalidPassword(errorMessage = "Vyplňte prosím heslo.").also { okPassword = false }

        return okEmail && okPassword
    }

    private val TAG = "LoginFragment"

}