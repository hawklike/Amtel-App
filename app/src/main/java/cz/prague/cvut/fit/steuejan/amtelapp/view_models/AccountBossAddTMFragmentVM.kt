package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.NameConverter
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch

class AccountBossAddTMFragmentVM : ViewModel()
{
    private val userCreated = SingleLiveEvent<RegistrationState>()
    fun isUserCreated(): LiveData<RegistrationState> = userCreated

    /*---------------------------------------------------*/

    private val credentials = MutableLiveData<CredentialsState>()
    fun isCredentialsValid(): LiveData<CredentialsState> = credentials

    /*---------------------------------------------------*/

    fun createUser(context: Context, credentials: ValidCredentials)
    {
        val password = NameConverter.getRandomString(6)
        viewModelScope.launch {
            val uid = AuthManager.signUpUser(context, credentials.email, password)
            if(uid == null) userCreated.value = InvalidRegistration
            else userCreated.value = ValidRegistration(uid, password, credentials)
        }
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

        if(okName && okSurname && okEmail) credentials.value = ValidCredentials(cName, cSurname, email)
        else credentials.value = InvalidCredentials(okName, okSurname, okEmail)
    }
}