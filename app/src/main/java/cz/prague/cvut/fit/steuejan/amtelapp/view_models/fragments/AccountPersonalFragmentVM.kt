package cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Message
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.UserRepository
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toCalendar
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toDate
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Sex
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch
import java.util.*

class AccountPersonalFragmentVM : ViewModel()
{
    private val passwordState = MutableLiveData<PasswordState>()
    fun confirmPassword(): LiveData<PasswordState> = passwordState

    /*---------------------------------------------------*/

    private val passwordChange = SingleLiveEvent<Boolean>()
    fun isPasswordChanged(): LiveData<Boolean> = passwordChange

    /*---------------------------------------------------*/

    private val nameState = MutableLiveData<NameState>()
    fun confirmName(): LiveData<NameState> = nameState

    /*---------------------------------------------------*/
    
    private val birthdateState = MutableLiveData<BirthdateState>()
    fun confirmBirthdate(): LiveData<BirthdateState> = birthdateState

    /*---------------------------------------------------*/

    private val phoneNumberState = MutableLiveData<PhoneNumberState>()
    fun confirmPhoneNumber(): LiveData<PhoneNumberState> = phoneNumberState

    /*---------------------------------------------------*/

    private val personalInfoChange = SingleLiveEvent<PersonalInfoState>()
    fun isPersonalInfoChanged(): LiveData<PersonalInfoState> = personalInfoChange

    /*---------------------------------------------------*/

    private val emailChange = SingleLiveEvent<EmailState>()
    fun isEmailChanged(): LiveData<EmailState> = emailChange

    /*---------------------------------------------------*/

    private val userUpdated = SingleLiveEvent<Boolean>()
    fun isUserUpdated(): LiveData<Boolean> = userUpdated

    /*---------------------------------------------------*/

    private val updatedTeam = SingleLiveEvent<Team>()
    fun isTeamUpdated(): LiveData<Team> = updatedTeam

    /*---------------------------------------------------*/

    fun confirmPassword(newPassword: String, confirmation: String)
    {
        passwordState.value = PasswordState.validate(newPassword, confirmation)
    }

    fun addNewPassword(newPassword: String)
    {
        viewModelScope.launch {
            passwordChange.value = AuthManager.changePassword(newPassword) is ValidPassword
        }
    }

    fun createAfterPasswordChangeDialog(success: Boolean): Pair<String, String?>
    {
        val title: String
        val message: String?

        if(success)
        {
            title = App.context.getString(R.string.password_change_success_title)
            message = null
        }
        else
        {
            title = App.context.getString(R.string.password_change_failure_title)
            message = "Uběhla již nějaká doba od posledního přihlášení. Prosím odhlašte se a zkuste to znovu."
        }

        return Pair(title, message)
    }

    fun savePersonalInfo(user: User, fullName: String, birthdate: String, phoneNumber: String, sex: Sex)
    {
        if(confirmPersonalInfo(fullName, birthdate, phoneNumber))
        {
            val phone: String? = if(phoneNumber.isEmpty()) null else phoneNumber
            val name = fullName.split(Regex("[ ]+")).first()
            val surname = fullName.split(Regex("[ ]+")).drop(1).joinToString(" ")

            viewModelScope.launch {

                user.apply {
                    this.name = name
                    this.surname = surname
                    this.birthdate = birthdate.toDate()
                    this.phone = phone
                    this.sex = sex.toBoolean()
                    this.firstSign = false
                }

                val success = UserRepository.setUser(user)?.let {
                    TeamRepository.updateUserInTeam(user)
                }

                if(success == true) personalInfoChange.value = PersonalInfoSuccess(name, surname, birthdate, phone, sex)
                else personalInfoChange.value = PersonalInfoFailure
            }
        }
    }

    private fun confirmPersonalInfo(fullName: String, birthdate: String, phoneNumber: String): Boolean
    {
        var okFullName = true
        var okBirthdate = true
        var okPhoneNumber = true
        
        if(fullName.isEmpty() || fullName.split(Regex("[ ]+")).size < 2)
        {
            okFullName = false
            nameState.value = InvalidName(App.context.getString(R.string.invalid_fullName_error))
        }

        with(BirthdateState.validate(birthdate)) {
            if(this is InvalidBirthdate)
            {
                okBirthdate = false
                birthdateState.value = this
            }
        }

        with(PhoneNumberState.validate(phoneNumber)) {
            if(this is InvalidPhoneNumber)
            {
                okPhoneNumber = false
                phoneNumberState.value = this
            }
        }

        return okFullName && okBirthdate && okPhoneNumber
    }

    fun setDialogBirthdate(birthdate: Editable): Calendar?
    {
        return if(birthdate.isEmpty()) null
        else birthdate.toString().toCalendar()
    }

    fun confirmEmail(email: String): Boolean
            = EmailState.validate(email) is ValidEmail

    fun changeEmail(newEmail: String)
    {
        viewModelScope.launch {
            emailChange.value = AuthManager.changeEmail(newEmail)
        }
    }

    fun updateUserEmail(user: User, email: String)
    {
        viewModelScope.launch {
            user.email = email
            UserRepository.setUser(user)?.let { user ->
                user.teamId?.let {
                    TeamRepository.updateUserInTeam(user)
                }
                userUpdated.value = true
            }
            ?: let { userUpdated.value = false }
        }
    }

    fun updateTeam(teamId: String?)
    {
        teamId.let {
            viewModelScope.launch {
                val team = TeamRepository.findTeam(teamId)
                if(team is ValidTeam) updatedTeam.value = team.self
            }
        }
    }

}