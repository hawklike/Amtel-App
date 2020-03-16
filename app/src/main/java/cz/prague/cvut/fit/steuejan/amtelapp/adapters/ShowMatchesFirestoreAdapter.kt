package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.App
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
        private val card: RelativeLayout = itemView.findViewById(R.id.match_card)
        val home: TextView = itemView.findViewById(R.id.match_card_home)
        val away: TextView = itemView.findViewById(R.id.match_card_away)
        val sets: TextView = itemView.findViewById(R.id.match_card_sets)

        init
        {
            card.setOnClickListener {
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int, match: Match)
    {
        holder.home.text = match.home
        holder.away.text = match.away
        holder.sets.text = match.homeScore?.let { "$it : ${match.awayScore}" } ?: "N/A"

        setColors(holder, match)
    }

    private fun setColors(holder: ViewHolder, match: Match)
    {
        if(user is SignedUser && user.self.teamId != null)
        {
            val teamId = user.self.teamId!!
            if(teamId == match.homeId || teamId == match.awayId)
            {
                holder.home.setTextColor(App.getColor(R.color.blue))
                holder.away.setTextColor(App.getColor(R.color.blue))
                holder.sets.setTextColor(App.getColor(R.color.blue))
            }
        }
    }
}