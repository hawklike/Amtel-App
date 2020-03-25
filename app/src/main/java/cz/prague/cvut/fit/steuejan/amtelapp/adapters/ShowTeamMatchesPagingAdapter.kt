package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team

class ShowTeamMatchesPagingAdapter(private val team: Team, options: FirestorePagingOptions<Match>)
    : FirestorePagingAdapter<Match, ShowTeamMatchesPagingAdapter.ViewHolder>(options)
{
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val homeName: TextView = itemView.findViewById(R.id.match_card_square_home)
        val awayName: TextView = itemView.findViewById(R.id.match_card_square_away)
        val sets: TextView = itemView.findViewById(R.id.match_card_square_sets)
        val result: Button = itemView.findViewById(R.id.match_card_square_result)
        val date: TextView = itemView.findViewById(R.id.match_card_square_date)
        private val card: RelativeLayout = itemView.findViewById(R.id.match_card_square)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.match_card_square, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, match: Match)
    {
        val isHomeTeam = team.id == match.homeId

        holder.homeName.text = match.home
        holder.awayName.text = match.away
        holder.sets.text = match.homeScore?.let { "$it : ${match.awayScore}" } ?: "N/A"

        getWinner(holder, isHomeTeam, match)

        holder.date.text = match.dateAndTime?.toMyString() ?: "datum neznámé"
    }

    private fun getWinner(holder: ViewHolder, isHomeTeam: Boolean, match: Match)
    {
        when
        {
            match.homeScore == null -> setResultButton(holder, "-", R.color.lightGrey)
            isHomeTeam && match.homeScore!! > match.awayScore!! -> setResultButton(holder, "V", R.color.blue)
            !isHomeTeam && match.awayScore!! > match.homeScore!! -> setResultButton(holder, "V", R.color.blue)
            else -> setResultButton(holder, "P", R.color.red)
        }
    }

    private fun setResultButton(holder: ViewHolder, text: String, @ColorRes colorRes: Int)
    {
        holder.result.text = text
        holder.result.backgroundTintList = ColorStateList.valueOf(App.getColor(colorRes))
    }

}