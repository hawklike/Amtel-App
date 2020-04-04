package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Player
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import java.util.*

class ShowPlayersAdapter(
    private val list: List<Player>,
    private val teamProfile: Boolean = false,
    private val winners: List<Boolean?> = emptyList(),
    private val team: Team? = null
) : RecyclerView.Adapter<ShowPlayersAdapter.ViewHolder>()
{
    var onClick: ((player: Player) -> Unit)? = { toast(R.string.not_working_yet) }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val name: TextView = itemView.findViewById(R.id.player_card_square_name)
        val surname: TextView = itemView.findViewById(R.id.player_card_square_surname)
        val footer: TextView = itemView.findViewById(R.id.player_card_square_footer)
        val result: Button = itemView.findViewById(R.id.player_card_square_result)
        private val card: RelativeLayout = itemView.findViewById(R.id.player_card_square)

        init
        {
            card.setOnClickListener { onClick?.invoke(getItem(adapterPosition)) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.player_card_square, parent, false)
        return ViewHolder(view)
    }

    private fun getItem(position: Int): Player = list[position]

    private fun isWinner(position: Int): Boolean? = winners[position]

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val player = getItem(position)
        holder.name.text = player.name
        holder.surname.text = player.surname
        if(teamProfile)
        {
            holder.footer.text = String.format(context.getString(R.string.years), DateUtil.getAge(player.birthdate ?: Date()))
            if(player.playerId == team?.tmId)
            {
                holder.name.setTextColor(App.getColor(R.color.blue))
                holder.surname.setTextColor(App.getColor(R.color.blue))
            }
        }
        else
        {
            holder.footer.text = player.isHome?.let {
                if(it) context.getString(R.string.home) else context.getString(R.string.away)
            } ?: ""
            with(holder.footer) { this.setTypeface(this.typeface, Typeface.BOLD) }
            getWinner(holder, position)
        }
    }

    private fun getWinner(holder: ViewHolder, position: Int)
    {
        holder.result.visibility = View.VISIBLE

        when(isWinner(position))
        {
            null -> setResultButton(holder, "-", R.color.lightGrey)
            true -> setResultButton(holder, context.getString(R.string.winner_acronym), R.color.blue)
            else -> setResultButton(holder, context.getString(R.string.loser_acronym), R.color.red)
        }
    }

    private fun setResultButton(holder: ViewHolder, text: String, @ColorRes colorRes: Int)
    {
        holder.result.text = text
        holder.result.backgroundTintList = ColorStateList.valueOf(App.getColor(colorRes))
    }
}