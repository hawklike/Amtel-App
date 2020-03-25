package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Player
import java.util.*

class ShowPlayersAdapter(private val list: List<Player>, private val teamProfile: Boolean = false)
    : RecyclerView.Adapter<ShowPlayersAdapter.ViewHolder>()
{
    var onClick: (player: Player) -> Unit = { toast(R.string.not_working_yet) }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val name: TextView = itemView.findViewById(R.id.player_card_square_name)
        val surname: TextView = itemView.findViewById(R.id.player_card_square_surname)
        val footer: TextView = itemView.findViewById(R.id.player_card_square_footer)
        private val card: RelativeLayout = itemView.findViewById(R.id.player_card_square)

        init
        {
            card.setOnClickListener { onClick.invoke(getItem(adapterPosition)) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.player_card_square, parent, false)
        return ViewHolder(view)
    }

    private fun getItem(position: Int): Player = list[position]

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val player = getItem(position)
        holder.name.text = player.name
        holder.surname.text = player.surname
        if(teamProfile)
        {
            holder.footer.text = String.format(context.getString(R.string.years), DateUtil.getAge(player.birthdate ?: Date()))
        }
        else
        {
            holder.footer.text = player.isHome?.let {
                if(it) context.getString(R.string.home) else context.getString(R.string.away)
            } ?: ""
            with(holder.footer) { this.setTypeface(this.typeface, Typeface.BOLD) }
        }
    }
}