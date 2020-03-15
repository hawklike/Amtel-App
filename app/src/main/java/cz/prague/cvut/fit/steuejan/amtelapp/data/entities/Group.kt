package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group(var name: String = "",
                 var teamIds: MutableList<String> = mutableListOf(),
                 var rounds: MutableMap<String, Int> = mutableMapOf(DateUtil.actualYear.toString() to 0),
                 var roundDates: MutableMap<String, Int> = mutableMapOf()
                 ) : Parcelable, Entity()