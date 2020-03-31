package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import kotlinx.android.parcel.Parcelize

@Parcelize
data class League(override var id: String? = "league",
                  var actualSeason: Int = DateUtil.actualSeason.toInt())
                  : Parcelable, Entity<League>()