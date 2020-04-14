package cz.prague.cvut.fit.steuejan.amtelapp.states

import android.os.Parcelable
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import kotlinx.android.parcel.Parcelize

sealed class UserState

@Parcelize
data class SignedUser(var self: User, val firstSign: Boolean = false) : UserState(), Parcelable
@Parcelize
object NoUser : UserState(), Parcelable
@Parcelize
object DeletedUser: UserState(), Parcelable