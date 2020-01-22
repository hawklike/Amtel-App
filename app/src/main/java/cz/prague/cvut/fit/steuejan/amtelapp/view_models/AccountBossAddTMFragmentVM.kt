package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.EmailSender
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.NameConverter
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.states.CredentialsState
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidCredentials
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidCredentials

class AccountBossAddTMFragmentVM : ViewModel()
{
    private val userCreated = SingleLiveEvent<Boolean>()
    fun isUserCreated(): LiveData<Boolean> = userCreated

    /*---------------------------------------------------*/

    private val credentials = MutableLiveData<CredentialsState>()
    fun isCredentialsValid(): LiveData<CredentialsState> = credentials

    /*---------------------------------------------------*/

    fun createUser(context: Context, name: String, surname: String, email: String)
    {
        val password = NameConverter.getRandomString(6)
        AuthManager.signUpUser(context, email, password, object: AuthManager.FirebaseUserListener
        {
            override fun onSignUpCompleted(uid: String?)
            {
                uid?.let {
                    userCreated.value = true
                    UserManager.addUser(it, name, surname, email, UserRole.TEAM_MANAGER)
                }
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

        if(okName && okSurname && okEmail) credentials.value = ValidCredentials(cName, cSurname, email)
        else credentials.value = InvalidCredentials(okName, okSurname, okEmail)
    }

    private fun sendEmail(context: Context, email: String, password: String)
    {
        EmailSender.sendVerificationEmail(context, email, password)
    }
}