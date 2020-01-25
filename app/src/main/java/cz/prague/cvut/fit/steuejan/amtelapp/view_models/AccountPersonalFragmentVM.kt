package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.PhoneNumberValidator
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Sex
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch

class AccountPersonalFragmentVM : ViewModel()
{
    private val password = MutableLiveData<PasswordState>()
    fun confirmPassword(): LiveData<PasswordState> = password

    /*---------------------------------------------------*/

    private val passwordChange = SingleLiveEvent<Boolean>()
    fun isPasswordChanged(): LiveData<Boolean> = passwordChange

    /*---------------------------------------------------*/

    private val birthdate = MutableLiveData<BirthdateState>()
    fun confirmBirthdate(): LiveData<BirthdateState> = birthdate

    /*---------------------------------------------------*/

    private val phoneNumber = MutableLiveData<PhoneNumberState>()
    fun confirmPhoneNumber(): LiveData<PhoneNumberState> = phoneNumber

    /*---------------------------------------------------*/

    private val personalInfoChange = SingleLiveEvent<PersonalInfoState>()
    fun isPersonalInfoChanged(): LiveData<PersonalInfoState> = personalInfoChange

    /*---------------------------------------------------*/

    //TODO: add error messages as parameters
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

    fun createAfterPasswordChangeDialog(success: Boolean,
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

    fun savePersonalInfo(birthdate: String, phoneNumber: String, sex: Sex)
    {
        if(confirmPersonalInfo(birthdate, phoneNumber))
        {
            val phone: String? = if(phoneNumber.isEmpty()) null else phoneNumber
            viewModelScope.launch {
                val success = UserManager.updateUser(AuthManager.currentUser!!.uid, mapOf(
                    "birthdate" to DateUtil.stringToDate(birthdate, "dd.MM.yyyy"),
                    "phone" to phone,
                    "sex" to Sex.toBoolean(sex)
                ))

                if(success) personalInfoChange.value = PersonalInfoSuccess(birthdate, phone, sex)
                else personalInfoChange.value = PersonalInfoFailure
            }
        }
    }

    //TODO: add error messages as parameters
    private fun confirmPersonalInfo(birthdate: String, phoneNumber: String): Boolean
    {
        var okBirthdate = true
        var okPhoneNumber = true

        if(!DateUtil.validateDate(birthdate, "dd.MM.yyyy"))
        {
            okBirthdate = false
            this.birthdate.value = InvalidBirthdate(errorMessage = "Vyplňte prosím datum narození ve formátu dd.MM.yyyy")
        }

        try
        {
            if(!DateUtil.validateBirthdate(birthdate, "dd.MM.yyyy"))
            {
                okBirthdate = false
                this.birthdate.value = InvalidBirthdate(errorMessage = "Datum narození neodpovídá realitě.")
            }
        }
        catch(ex: Exception) { }

        if(phoneNumber.isNotEmpty() && !PhoneNumberValidator.isValid(phoneNumber, "CZ", "SK", "PL"))
        {
            okPhoneNumber = false
            this.phoneNumber.value = InvalidPhoneNumber
        }

        return okBirthdate && okPhoneNumber
    }

    fun createAfterPersonalInfoDialog(state: PersonalInfoState, successTitle: String, unsuccessTitle: String): String
    {
        return if(state is PersonalInfoSuccess) successTitle
        else unsuccessTitle
    }

}