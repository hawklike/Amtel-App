package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.LeagueManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class League(override var id: String? = LeagueManager.leagueId,
                  var actualSeason: Int = DateUtil.actualSeason.toInt(),
                  var deadlineFrom: MutableMap<String, Date> = mutableMapOf(),
                  var deadlineTo: MutableMap<String, Date> = mutableMapOf(),
                  @ServerTimestamp
                  var serverTime: Date? = null
                  ) : Parcelable, Entity<League>()