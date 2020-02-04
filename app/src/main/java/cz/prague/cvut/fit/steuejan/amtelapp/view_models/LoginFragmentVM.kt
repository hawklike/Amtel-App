package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.Message
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch

class LoginFragmentVM : ViewModel()
{
    private val emailState = MutableLiveData<EmailState>()
    fun confirmEmail(): LiveData<EmailState> = emailState

    /*---------------------------------------------------*/

    private val passwordState = MutableLiveData<PasswordState>()
    fun confirmPassword(): LiveData<PasswordState> = passwordState

    /*---------------------------------------------------*/

    private val userState = SingleLiveEvent<UserState>()
    fun getUser(): LiveData<UserState> = userState

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
                    userState.value = user?.let { SignedUser(it, user.firstSign) } ?: NoUser
                    user?.let {
                        if(user.firstSign)
                            UserManager.updateUser(user.id!!, mapOf("firstSign" to false))
                    }
                }
                else userState.value = NoUser
            }
        }
    }

    fun createAfterDialog(user: UserState): Message
    {
        val title: String
        val message: String?

        if(user is SignedUser)
        {
            title = App.context.getString(R.string.user_login_success_title)
            message = when
            {
                UserRole.toRole(user.self.role) != UserRole.TEAM_MANAGER -> null
                user.firstSign -> App.context.getString(R.string.user_login_success_message)
                else -> null
            }
            Log.i(TAG, "getUser(): login was successful - current user: $user")
        }
        else
        {
            title = App.context.getString(R.string.user_login_failure_title)
            message = App.context.getString(R.string.user_login_failure_message)
            Log.e(TAG, "getUser(): login not successful")
        }

        return Message(title, message)
    }

    private fun confirmCredentials(email: String, password: String): Boolean
    {
        var okEmail = true
        var okPassword = true

        with(EmailState.validate(email)) {
            if(this is InvalidEmail) okEmail = false
            emailState.value = this
        }

        with(PasswordState.validate(password)) {
            if(this is InvalidPassword) okPassword = false
            passwordState.value = this
        }

        return okEmail && okPassword
    }

    private val TAG = "LoginFragment"

}