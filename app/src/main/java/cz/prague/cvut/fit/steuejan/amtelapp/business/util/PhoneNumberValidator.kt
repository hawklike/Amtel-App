package cz.prague.cvut.fit.steuejan.amtelapp.business.util

import android.util.Log
import com.google.i18n.phonenumbers.PhoneNumberUtil

object PhoneNumberValidator
{
    fun isValid(phoneNumber: String, vararg countryCode: String): Boolean
    {
        var result = true
        val util = PhoneNumberUtil.getInstance()
        countryCode.forEach { code ->
            try
            {
                val numberProto = util.parse(phoneNumber, code)
                result = util.isValidNumber(numberProto) && result
            }
            catch(ex: Exception) { return false }
        }
        Log.i("PhoneNumberValidator", "isValid(): is $phoneNumber valid? $result")
        return result
    }
}