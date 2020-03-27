package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowGroupsMenuAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import java.util.*

class ShowGroupsMenuFirestoreAdapterVM : ViewModel()
{
    fun getActualRound(group: Group): Int?
    {
        val week = DateUtil.getWeekNumber(Date())
        return try { group.roundDates.filterValues { it == week }.keys.first().toInt() }
        catch(ex: Exception) { null }
    }

    @SuppressLint("SetTextI18n")
    fun getPlayOffDate(holder: ShowGroupsMenuAdapter.ViewHolder, group: Group)
    {
        holder.rounds.visibility = View.INVISIBLE

        group.roundDates["playOff"]?.let { weekNumber ->
            val firstWeek = DateUtil.getWeekDate(weekNumber)
            val secondWeek = DateUtil.getWeekDate(weekNumber + 1)
            val week = firstWeek + secondWeek
            week.find { DateUtil.compareDates(it, Date()) == 0 } ?: disableCard(holder)
            holder.actualRound.text = "${week.first().toMyString()} – ${week.last().toMyString()}"
        }
    }

    fun disableCard(holder: ShowGroupsMenuAdapter.ViewHolder)
    {
        holder.card.isEnabled = false
        holder.card.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.veryLightGrey))
        holder.card.elevation = 0.0F
        holder.name.setTextColor(App.getColor(R.color.lightGrey))
        holder.rounds.setTextColor(App.getColor(R.color.lightGrey))
        holder.logo.setTextColor(App.getColor(R.color.lightGrey))
        holder.actualRound.setTextColor(App.getColor(R.color.lightGrey))
    }

    fun isRanking(holder: ShowGroupsMenuAdapter.ViewHolder, group: Group)
    {
        if(group.playOff) holder.card.visibility = View.GONE
        holder.actualRound.text = String.format(App.context.getString(R.string.last_active_season),
            group.teamIds.keys.map { it.toInt() }.max() ?: App.context.getString(R.string.is_not))
        holder.rounds.text = String.format(
            App.context.getString(R.string.teams_number_this_year),
            group.teamIds[DateUtil.actualYear]?.size ?: 0)
        if(group.teamIds.isEmpty()) disableCard(holder)
    }

}
