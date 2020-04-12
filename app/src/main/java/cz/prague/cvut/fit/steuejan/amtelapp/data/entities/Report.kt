package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Report(override var id: String? = null,
                  var title: String = "",
                  var lead: String = "",
                  var text: String = "",
                  var date: Date = Date()
                  ) : Entity<Report>(), Parcelable