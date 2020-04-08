package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Message(val fullname: String = "",
                   val messageText: String = "",
                   val fromId: String = "",
                   @ServerTimestamp
                   val sentAt: Date? = null) : Entity<Message>(), Parcelable