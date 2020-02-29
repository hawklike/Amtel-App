package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.UserState

class ShowMatchesFirestoreAdapter(private val user: UserState, options: FirestoreRecyclerOptions<Match>)
    : FirestoreRecyclerAdapter<Match, ShowMatchesFirestoreAdapter.ViewHolder>(options)
{
    var onNextClickOwner: (match: Match) -> Unit = {}
    var onNextClickGuest: (match: Match) -> Unit = {}

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val home: TextView = itemView.findViewById(R.id.match_card_home)
        val away: TextView = itemView.findViewById(R.id.match_card_away)
        val sets: TextView = itemView.findViewById(R.id.match_card_sets)
        val gems: TextView = itemView.findViewById(R.id.match_card_gems)
        val upperText: TextView = itemView.findViewById(R.id.match_card_upper_text)
        val lowerText: TextView = itemView.findViewById(R.id.match_card_lower_text)
        val next: ImageButton = itemView.findViewById(R.id.match_card_next)

        init
        {
            next.setOnClickListener {
                val match = getItem(adapterPosition)

                if(user is SignedUser)
                {
                    val teamId = user.self.teamId
                    val condition = user.self.role.toRole() == UserRole.HEAD_OF_LEAGUE ||
                            (teamId != null && (teamId == match.homeId || teamId == match.awayId))

                    if(condition) onNextClickOwner.invoke(match)
                    else onNextClickGuest.invoke(match)
                }
                else onNextClickGuest.invoke(match)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.match_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, match: Match)
    {
        holder.home.text = match.home
        holder.away.text = match.away
        holder.sets.text = match.homeScore?.let { "$it : ${match.awayScore}" } ?: "N/A"

        holder.gems.visibility = View.GONE
        holder.upperText.visibility = View.GONE
        holder.lowerText.visibility = View.GONE

        setColors(holder, match)
    }

    private fun setColors(holder: ViewHolder, match: Match)
    {
        if(user is SignedUser && user.self.teamId != null)
        {
            val teamId = user.self.teamId!!
            if(teamId == match.homeId || teamId == match.awayId)
            {
                holder.home.setTextColor(ContextCompat.getColor(context, R.color.blue))
                holder.away.setTextColor(ContextCompat.getColor(context, R.color.blue))
                holder.sets.setTextColor(ContextCompat.getColor(context, R.color.blue))
            }
        }
    }
}