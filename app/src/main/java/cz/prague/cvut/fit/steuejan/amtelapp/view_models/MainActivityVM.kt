package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import kotlinx.android.parcel.Parcelize

class MainActivityVM(private val state: SavedStateHandle) : ViewModel()
{
    fun setTitle(title: String)
    {
        state.set(TITLE, title)
    }

    fun getTitle(): LiveData<String> = state.getLiveData(TITLE)

    /*---------------------------------------------------*/

    fun setDrawerSelectedPosition(position: Int)
    {
        state.set(DRAWER_POSITION, position)
    }

    fun getDrawerSelectedPosition(): Int = state[DRAWER_POSITION] ?: 0

    /*---------------------------------------------------*/

    private val user = SingleLiveEvent<UserStatus>()

    //TODO: change to a User
    fun setUser(user: UserStatus)
    {
        this.user.value = user
    }

    fun getUser(): LiveData<UserStatus> = user

    sealed class UserStatus
    {
        @Parcelize
        data class SignedUser(val self: FirebaseUser) : UserStatus(), Parcelable
        @Parcelize object NoUser : UserStatus(), Parcelable
    }

    companion object
    {
        const val TITLE = "title"
        const val DRAWER_POSITION = "position"
        const val USER = "user"
    }
}