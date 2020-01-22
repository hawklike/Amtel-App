package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.utils.NameConverter

class AccountBossAddTMFragmentVM : ViewModel()
{
    private val name = MutableLiveData<NameState>()
    fun confirmName(): LiveData<NameState> = name

    /*---------------------------------------------------*/

    private val surname = MutableLiveData<SurnameState>()
    fun confirmSurname(): LiveData<SurnameState> = surname

    /*---------------------------------------------------*/


    private val email = MutableLiveData<EmailState>()
    fun confirmEmail(): LiveData<EmailState> = email

    /*---------------------------------------------------*/

    private val userCreated = SingleLiveEvent<Boolean>()
    fun isUserCreated(): LiveData<Boolean> = userCreated

    /*---------------------------------------------------*/

    private val credentials = SingleLiveEvent<CredentialsState>()
    fun isCredentialsValid(): LiveData<CredentialsState> = credentials

    /*---------------------------------------------------*/

    //TODO: create random password and send it via email
    fun createUser(context: Context, name: String, surname: String, email: String)
    {
        if(confirmCredentials(name, surname, email))
            AuthManager.signUpUser(context, email, "hawklike", object: AuthManager.FirebaseUserListener
            {
                override fun onSignUpCompleted(success: Boolean)
                {
                   userCreated.value = success
                }
            })
    }

    fun confirmCredentials(name: String, surname: String, email: String): Boolean
    {
        var okName = true
        var okSurname = true
        var okEmail = true

        var cName = ""
        var cSurname = ""

        if(name.isNotEmpty())
        {
            cName = NameConverter.convertToFirstLetterBig(name)
            this.name.value = NameState.ValidName(cName)
        }
        else
            this.name.value = NameState.InvalidName.also { okName = false }

        if(surname.isNotEmpty())
        {
            cSurname = NameConverter.convertToFirstLetterBig(surname)
            this.surname.value = SurnameState.ValidSurname(cSurname)
        }
        else
            this.surname.value = SurnameState.InvalidSurname.also { okSurname = false }

        if(email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
            this.email.value = EmailState.ValidEmail(email)
        else
            this.email.value = EmailState.InvalidEmail.also { okEmail = false }

        return (okName && okSurname && okEmail).also { isValid ->
            if(isValid) credentials.value = CredentialsState.ValidCredentials(cName, cSurname, email)
            else credentials.value = CredentialsState.InvalidCredentials
        }
    }



    sealed class CredentialsState
    {
        data class ValidCredentials(val name: String, val surname: String, val email: String) : CredentialsState()
        object InvalidCredentials : CredentialsState()
    }

    sealed class NameState
    {
        data class ValidName(val name: String) : NameState()
        object InvalidName : NameState()
    }

    sealed class SurnameState
    {
        data class ValidSurname(val surname: String) : SurnameState()
        object InvalidSurname : SurnameState()
    }

    sealed class EmailState
    {
        data class ValidEmail(val email: String) : EmailState()
        object InvalidEmail : EmailState()
    }



}