package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match

class ShowMatchesFirestoreAdapter(private val context: Context, options: FirestoreRecyclerOptions<Match>)
    : FirestoreRecyclerAdapter<Match, ShowMatchesFirestoreAdapter.ViewHolder>(options)
{
//    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(MatchAdapterVM::class.java)

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
                Toast.makeText(context, context.getString(R.string.not_working_yet), Toast.LENGTH_SHORT).show()
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
        holder.sets.text = match.homeSets?.let { "$it:${match.awaySets}" } ?: "N/A"

        holder.gems.visibility = View.GONE
        holder.upperText.visibility = View.GONE
        holder.lowerText.visibility = View.GONE
    }

}