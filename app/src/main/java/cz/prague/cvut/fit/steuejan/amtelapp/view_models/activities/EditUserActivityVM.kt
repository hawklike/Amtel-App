package cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.removeWhitespaces
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toCalendar
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toDate
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.UserRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch
import java.util.*

class EditUserActivityVM : ViewModel()
{
    var user: User? = null

    var deleteRole: Boolean = false

    /*---------------------------------------------------*/

    private val _phoneNumber = MutableLiveData<PhoneNumberState>()
    val phoneNumber: LiveData<PhoneNumberState> = _phoneNumber

    /*---------------------------------------------------*/

    private val _name = MutableLiveData<NameState>()
    val name: LiveData<NameState> = _name

    /*---------------------------------------------------*/

    private val _surname = MutableLiveData<SurnameState>()
    val surname: LiveData<SurnameState> = _surname

    /*---------------------------------------------------*/

    private val _email = MutableLiveData<EmailState>()
    val email: LiveData<EmailState> = _email

    /*---------------------------------------------------*/

    private val _birthdate = MutableLiveData<BirthdateState>()
    val birthdate: LiveData<BirthdateState> = _birthdate

    /*---------------------------------------------------*/

    private val _userEdited = SingleLiveEvent<Boolean>()
    val userEdited: LiveData<Boolean> = _userEdited

    /*---------------------------------------------------*/

    fun editUser(name: String, surname: String, email: String, phone: String, birthdate: String)
    {
        viewModelScope.launch {
            if(confirmInput(name, surname, email, phone, birthdate))
            {
                val phoneTrimmed = phone.removeWhitespaces().let {
                    if(it.isEmpty()) null
                    else it
                }

                user?.apply {
                    this.name = name
                    this.surname = surname
                    this.email = email
                    this.phone = phoneTrimmed
                    this.birthdate = birthdate.toDate()
                    this.firstSign = user?.firstSign ?: false
                }

                if(deleteRole) user?.role = UserRole.PLAYER.toString()

                val success = UserRepository.setUser(user)?.let {
                    TeamRepository.updateUserInTeam(user)
                }

                _userEdited.value = success == true
            }
        }
    }

    private fun confirmInput(name: String, surname: String, email: String, phone: String, birthdate: String): Boolean
    {
        var ok = true

        if(name.isBlank())
        {
            ok = false
            _name.value = InvalidName()
        }

        if(surname.isBlank())
        {
            ok = false
            _surname.value = InvalidSurname()
        }

        EmailState.validate(email).let {
            if(it is InvalidEmail)
            {
                ok = false
                _email.value = it
            }
        }

        if(phone.isNotEmpty())
        {
            PhoneNumberState.validate(phone).let {
                if(it is InvalidPhoneNumber)
                {
                    ok = false
                    _phoneNumber.value = it
                }
            }
        }

        BirthdateState.validate(birthdate).let {
            if(it is InvalidBirthdate)
            {
                ok = false
                _birthdate.value = it
            }
        }

        return ok
    }

    fun setDialogBirthdate(birthdate: Editable): Calendar?
    {
        return if(birthdate.isEmpty()) null
        else birthdate.toString().toCalendar()
    }

}
