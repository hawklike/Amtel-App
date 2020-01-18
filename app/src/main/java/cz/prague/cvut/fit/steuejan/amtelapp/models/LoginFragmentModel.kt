package cz.prague.cvut.fit.steuejan.amtelapp.models

import android.os.Parcelable
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.android.parcel.Parcelize

class LoginFragmentModel : ViewModel()
{
    private val userState = MutableLiveData<UserState>()

    fun confirmLogin(email: String, password: String): LiveData<UserState>
    {
        if(email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            if(password.isEmpty()) userState.value =
                UserState.InvalidUser(email = false, password = false)

            else userState.value =
                UserState.InvalidUser(email = false, password = true)
        }
        else
            userState.value = UserState.ValidUser(email, password)

        return userState
    }

    companion object
    {
        private const val LOGIN_USER = "login"
    }

    sealed class UserState
    {
        @Parcelize
        data class ValidUser(val email: String, val password: String) : UserState(), Parcelable
        @Parcelize
        data class InvalidUser(val email: Boolean, val password: Boolean) : UserState(), Parcelable
    }
}