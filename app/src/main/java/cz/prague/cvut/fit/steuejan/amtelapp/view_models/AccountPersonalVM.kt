package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidPassword
import cz.prague.cvut.fit.steuejan.amtelapp.states.PasswordState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidPassword
import kotlinx.coroutines.launch

class AccountPersonalVM : ViewModel()
{
    private val password = MutableLiveData<PasswordState>()
    fun confirmPassword(): LiveData<PasswordState> = password

    /*---------------------------------------------------*/

    private val passwordChange = SingleLiveEvent<Boolean>()
    fun isPasswordChanged(): LiveData<Boolean> = passwordChange

    /*---------------------------------------------------*/

    fun confirmPassword(newPassword: String)
    {
        when(newPassword.length)
        {
            0 -> password.value = InvalidPassword(errorMessage = "Vyplňte prosím heslo.")
            in 1..5 -> password.value = InvalidPassword(errorMessage = "Heslo musí mít minimálně 6 znaků.")
            else -> password.value = ValidPassword(newPassword)
        }
    }

    fun addNewPassword(newPassword: String)
    {
        viewModelScope.launch {
            passwordChange.value = AuthManager.changePassword(newPassword) is ValidPassword
        }
    }

    fun createAfterDialog(success: Boolean,
                          successTitle: String,
                          unsuccessTitle: String,
                          unsuccessMessage: String)
    : Pair<String, String?>
    {
        val title: String
        val message: String?

        if(success)
        {
            title = successTitle
            message = null
        }
        else
        {
            title = unsuccessTitle
            message = unsuccessMessage
        }

        return Pair(title, message)
    }

}