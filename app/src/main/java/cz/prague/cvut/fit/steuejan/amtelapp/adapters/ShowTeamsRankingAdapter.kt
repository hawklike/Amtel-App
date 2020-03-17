package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.RankingOrderBy

class ShowTeamsRankingAdapter(private val list: List<Team>, private val year: String, private val orderBy: RankingOrderBy)
    : RecyclerView.Adapter<ShowTeamsRankingAdapter.ViewHolder>()
{
    var onClick: (player: Team) -> Unit = { toast(R.string.not_working_yet) }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val position: TextView = itemView.findViewById(R.id.ranking_team_card_rank)
        val name: TextView = itemView.findViewById(R.id.ranking_team_card_team)
        val matches: TextView = itemView.findViewById(R.id.ranking_team_card_matches)
        val wins: TextView = itemView.findViewById(R.id.ranking_team_card_wins)
        val losses: TextView = itemView.findViewById(R.id.ranking_team_card_losses)
        val positiveSets: TextView = itemView.findViewById(R.id.ranking_team_card_sets_positive)
        val negativeSets: TextView = itemView.findViewById(R.id.ranking_team_card_sets_negative)
        val points: TextView = itemView.findViewById(R.id.ranking_team_card_points)
        private val card: RelativeLayout = itemView.findViewById(R.id.ranking_team_card)

        init
        {
            card.setOnClickListener { onClick.invoke(getItem(adapterPosition)) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.ranking_team_card, parent, false)
        return ViewHolder(view)
    }

    private fun getItem(position: Int): Team = list[position]

    override fun getItemCount(): Int = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val team = getItem(position)
        holder.position.text = "${position + 1}."
        holder.name.text = team.name
        holder.matches.text = team.matchesPerYear[year]?.toString() ?: "0"
        holder.wins.text = team.winsPerYear[year]?.toString() ?: "0"
        holder.losses.text = team.lossesPerYear[year]?.toString() ?: "0"
        holder.positiveSets.text = team.positiveSetsPerYear[year]?.toString() ?: "0"
        holder.negativeSets.text = team.negativeSetsPerYear[year]?.toString() ?: "0"
        holder.points.text = team.pointsPerYear[year]?.toString() ?: "0"
        orderBy(holder, orderBy)
    }

    private fun orderBy(holder: ViewHolder, orderBy: RankingOrderBy)
    {
        when(orderBy)
        {
            RankingOrderBy.POINTS -> holder.points.setTextColor(App.getColor(R.color.blue))
            RankingOrderBy.MATCHES -> holder.matches.setTextColor(App.getColor(R.color.blue))
            RankingOrderBy.WINS -> holder.wins.setTextColor(App.getColor(R.color.blue))
            RankingOrderBy.LOSSES -> holder.losses.setTextColor(App.getColor(R.color.blue))
            RankingOrderBy.POSITIVE_SETS -> holder.positiveSets.setTextColor(App.getColor(R.color.blue))
            RankingOrderBy.NEGATIVE_SETS -> holder.negativeSets.setTextColor(App.getColor(R.color.blue))
        }
    }
}