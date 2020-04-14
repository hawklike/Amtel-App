package cz.prague.cvut.fit.steuejan.amtelapp.states

import android.util.Patterns
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R

sealed class EmailState
{
    companion object
    {
        fun validate(email: String): EmailState
        {
            return if(email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) ValidEmail(email)
            else InvalidEmail(App.context.getString(R.string.email_failure_message))
        }
    }
}
data class ValidEmail(val self: String) : EmailState()
data class InvalidEmail(val errorMessage: String = "") : EmailState()