package cz.prague.cvut.fit.steuejan.amtelapp.states

import android.os.Parcelable
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.parcel.Parcelize

sealed class UserState

//TODO: change to a User
@Parcelize
data class SignedUser(val self: FirebaseUser) : UserState(), Parcelable

@Parcelize
object NoUser : UserState(), Parcelable