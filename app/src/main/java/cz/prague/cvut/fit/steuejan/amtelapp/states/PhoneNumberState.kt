package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.PhoneNumberValidator

sealed class PhoneNumberState
{
    companion object
    {
        fun validate(phoneNumber: String): PhoneNumberState
        {
            return if(phoneNumber.isNotEmpty() && !PhoneNumberValidator.isValid(phoneNumber, "CZ", "SK", "PL"))
                InvalidPhoneNumber(App.context.getString(R.string.phoneNumber_invalid_message))
            else ValidPhoneNumber(phoneNumber)
        }
    }
}
data class ValidPhoneNumber(val self: String) : PhoneNumberState()
data class InvalidPhoneNumber(val errorMessage: String) : PhoneNumberState()