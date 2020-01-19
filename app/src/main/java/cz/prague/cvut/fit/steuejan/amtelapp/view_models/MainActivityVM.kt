package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

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

    //TODO: change to a User
    fun setUser(user: FirebaseUser)
    {
        state.set(USER, user)
    }

    fun getUser(): LiveData<FirebaseUser> = state.getLiveData(USER)

    companion object
    {
        const val TITLE = "title"
        const val DRAWER_POSITION = "position"
        const val USER = "user"
    }
}