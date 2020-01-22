package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.utils.EmailSender
import cz.prague.cvut.fit.steuejan.amtelapp.business.utils.NameConverter

class AccountBossAddTMFragmentVM : ViewModel()
{
    private val userCreated = SingleLiveEvent<Boolean>()
    fun isUserCreated(): LiveData<Boolean> = userCreated

    /*---------------------------------------------------*/

    private val credentials = MutableLiveData<CredentialsState>()
    fun isCredentialsValid(): LiveData<CredentialsState> = credentials

    /*---------------------------------------------------*/

    fun createUser(context: Context, email: String)
    {
        val password = NameConverter.getRandomString(6)
        AuthManager.signUpUser(context, email, password, object: AuthManager.FirebaseUserListener
        {
            override fun onSignUpCompleted(success: Boolean)
            {
                userCreated.value = success
                sendEmail(context, email, password)
            }
        })
    }

    fun confirmCredentials(name: String, surname: String, email: String)
    {
        var okName = true
        var okSurname = true
        var okEmail = true

        var cName = ""
        var cSurname = ""

        if(name.isNotEmpty()) cName = NameConverter.convertToFirstLetterBig(name)
        else okName = false

        if(surname.isNotEmpty()) cSurname = NameConverter.convertToFirstLetterBig(surname)
        else okSurname = false

        if(!email.isNotBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) okEmail = false

        if(okName && okSurname && okEmail) credentials.value = CredentialsState.ValidCredentials(cName, cSurname, email)
        else credentials.value = CredentialsState.InvalidCredentials(okName, okSurname, okEmail)
    }

    private fun sendEmail(context: Context, email: String, password: String)
    {
        EmailSender.sendVerificationEmail(context, email, password)
    }

    sealed class CredentialsState
    {
        data class ValidCredentials(val name: String, val surname: String, val email: String) : CredentialsState()
        data class InvalidCredentials(val name: Boolean, val surname: Boolean, val email: Boolean) : CredentialsState()
    }
}