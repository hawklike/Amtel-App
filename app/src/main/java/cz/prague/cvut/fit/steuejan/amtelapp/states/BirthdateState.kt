package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toDate
import java.util.*

sealed class BirthdateState
{
    companion object
    {
        fun validate(birthdate: String): BirthdateState
        {
            if(!DateUtil.validateDate(birthdate))
            {
                return InvalidBirthdate(App.context.getString(R.string.birthdate_failure_message_bad_format))
            }

            try
            {
                if(!DateUtil.validateBirthdate(birthdate))
                {
                    return InvalidBirthdate(App.context.getString(R.string.birthdate_failure_message_invalid))
                }
            }
            catch(ex: Exception) { }

            return ValidBirthdate(birthdate.toDate())
        }
    }
}

data class ValidBirthdate(val self: Date) : BirthdateState()
data class InvalidBirthdate(val errorMessage: String) : BirthdateState()