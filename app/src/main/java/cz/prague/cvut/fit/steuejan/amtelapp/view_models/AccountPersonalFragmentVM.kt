package cz.prague.cvut.fit.steuejan.amtelapp.view_models

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
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.NameConverter
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Sex
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch

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

    fun confirmPassword(newPassword: String)
    {
        passwordState.value = PasswordState.validate(newPassword)
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
            message = App.context.getString(R.string.password_change_failure_message)
        }

        return Pair(title, message)
    }

    fun savePersonalInfo(fullName: String, birthdate: String, phoneNumber: String, sex: Sex)
    {
        if(confirmPersonalInfo(fullName, birthdate, phoneNumber))
        {
            val phone: String? = if(phoneNumber.isEmpty()) null else phoneNumber
            val name = fullName.split(Regex("[ ]+"))[0]
            val surname = fullName.split(Regex("[ ]+"))[1]

            viewModelScope.launch {
                val success = UserManager.updateUser(AuthManager.currentUser!!.uid, mapOf(
                    "name" to NameConverter.convertToFirstLetterBig(name),
                    "surname" to NameConverter.convertToFirstLetterBig(surname),
                    "birthdate" to DateUtil.stringToDate(birthdate),
                    "phone" to phone,
                    "sex" to Sex.toBoolean(sex)
                ))

                if(success) personalInfoChange.value = PersonalInfoSuccess(name, surname, birthdate, phone, sex)
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

    fun createAfterPersonalInfoDialog(state: PersonalInfoState): Message
    {
        return if(state is PersonalInfoSuccess) Message(App.context.getString(R.string.personalInfo_change_success_title), null)
        else Message(App.context. getString(R.string.personalInfo_change_failure_title), null)
    }

}