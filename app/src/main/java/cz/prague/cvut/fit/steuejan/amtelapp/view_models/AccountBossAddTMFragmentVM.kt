package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.preference.PreferenceManager
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.LeagueManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.*
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch
import java.util.*

class AccountBossAddTMFragmentVM : ViewModel()
{
    private val _registration = SingleLiveEvent<RegistrationState>()
    val registration: LiveData<RegistrationState> = _registration

    /*---------------------------------------------------*/

    private val _credentials = SingleLiveEvent<CredentialsState>()
    val credentials: LiveData<CredentialsState> = _credentials

    /*---------------------------------------------------*/

    private val _deadlineAdded = SingleLiveEvent<Boolean>()
    val isDeadlineAdded: LiveData<Boolean> = _deadlineAdded

    /*---------------------------------------------------*/

    private val _deadlineDeleted = SingleLiveEvent<Boolean>()
    val isDeadlineDeleted: LiveData<Boolean> = _deadlineDeleted

    /*---------------------------------------------------*/

    private val _deadline = SingleLiveEvent<Pair<String?, String?>>()
    val deadline: LiveData<Pair<String?, String?>> = _deadline

    /*---------------------------------------------------*/

    fun createUser(credentials: ValidCredentials)
    {
        val password = StringUtil.getRandomString(6)

        viewModelScope.launch {
            val uid = AuthManager.signUpUser(credentials.email, password)

            uid?.let {
                val (name, surname, email) = credentials
                val user = User(uid, name, surname, email, role = UserRole.TEAM_MANAGER.toString())

                UserManager.setUser(user)?.let {
                    EmailSender.sendEmail(
                        email,
                        App.context.getString(R.string.verificationEmail_subject),
                        createVerificationTemplate(email, password))
                    _registration.value = ValidRegistration
                } ?: let { _registration.value = InvalidRegistration }

            } ?: let { _registration.value = InvalidRegistration }
        }
    }

    fun confirmCredentials(name: String, surname: String, email: String)
    {
        var okName = true
        var okSurname = true
        var okEmail = true

        var cName = ""
        var cSurname = ""

        if(name.isNotEmpty()) cName = name.firstLetterUpperCase()
        else okName = false

        if(surname.isNotEmpty()) cSurname = surname.firstLetterUpperCase()
        else okSurname = false

        if(EmailState.validate(email) is InvalidEmail) okEmail = false

        if(okName && okSurname && okEmail) _credentials.value = ValidCredentials(cName, cSurname, email)
        else _credentials.value = InvalidCredentials(okName, okSurname, okEmail)
    }

    private fun createVerificationTemplate(email: String, password: String): String
    {
        val head = App.context.getString(R.string.autoEmail_template_head)
        val body = "email: $email\nheslo: $password\n\n"
        val foot = App.context.getString(R.string.autoEmail_template_foot)
        return "$head$body$foot"
    }

    fun setDialogDeadline(birthdate: Editable): Calendar?
    {
        return if(birthdate.isEmpty()) null
        else birthdate.toString().toCalendar()
    }

    fun setDeadline(deadline: Date, from: Boolean)
    {
        viewModelScope.launch {
            with(LeagueManager.setDeadline(deadline, from)) {
                _deadlineAdded.value = this
                if(this) setDeadlineInPreferences(deadline.toMyString(), from)
            }
        }
    }

    fun getDeadline()
    {
        val deadlineFrom = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString("DEADLINE_FROM", null)

        val deadlineTo = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString("DEADLINE_TO", null)

        if(deadlineFrom == null || deadlineTo == null)
        {
            viewModelScope.launch {
                val result = LeagueManager.getDeadline()
                result?.let {
                    setDeadlineInPreferences(it.first?.toMyString(), true)
                    setDeadlineInPreferences(it.second?.toMyString(), false)
                    _deadline.value = Pair(it.first?.toMyString(), it.second?.toMyString())
                }
            }
        }
        else _deadline.value = Pair(deadlineFrom, deadlineTo)
    }

    private fun setDeadlineInPreferences(deadline: String?, from: Boolean)
    {
        val label =
            if(from) "DEADLINE_FROM"
            else "DEADLINE_TO"

        PreferenceManager.
            getDefaultSharedPreferences(context)
            .edit()
            .putString(label, deadline)
            .apply()
    }

    fun deleteDeadline()
    {
        viewModelScope.launch {
            val ok1 = LeagueManager.setDeadline(null, true)
            val ok2 = LeagueManager.setDeadline(null, false)
            setDeadlineInPreferences(null, true)
            setDeadlineInPreferences(null, false)
            _deadlineDeleted.value = ok1 && ok2
        }
    }

}