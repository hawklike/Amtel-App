package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class League(override var id: String? = "league",
                  var actualSeason: Int = DateUtil.actualSeason.toInt(),
                  var deadline: MutableMap<String, Date> = mutableMapOf()
                  ) : Parcelable, Entity<League>()