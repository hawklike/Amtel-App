package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Player

class ShowPlayersAdapter(private val list: List<Player>) : RecyclerView.Adapter<ShowPlayersAdapter.ViewHolder>()
{
    var onClick: (player: Player) -> Unit = { toast(R.string.not_working_yet) }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val name: TextView = itemView.findViewById(R.id.user_card_square_name)
        val surname: TextView = itemView.findViewById(R.id.user_card_square_surname)
        val membership: TextView = itemView.findViewById(R.id.user_card_square_homeAway)
        private val card: RelativeLayout = itemView.findViewById(R.id.user_card_square)

        init
        {
            card.setOnClickListener { onClick.invoke(getItem(adapterPosition)) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.user_card_square, parent, false)
        return ViewHolder(view)
    }

    private fun getItem(position: Int): Player = list[position]

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val player = getItem(position)
        holder.name.text = player.name
        holder.surname.text = player.surname
        holder.membership.text = if(player.isHome) context.getString(R.string.home) else context.getString(R.string.away)
    }
}