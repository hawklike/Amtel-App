package cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.getColor
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Player
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.MatchRepository
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.RoundCardSquareBinding

class ShowPlayerRoundsAdapter(private val rounds: List<Round>, private val player: Player)
    : RecyclerView.Adapter<ShowPlayerRoundsAdapter.ViewHolder>()
{
    var onClick: ((Round) -> Unit)? = null

    inner class ViewHolder(val binding: RoundCardSquareBinding) : RecyclerView.ViewHolder(binding.root)
    {
        init
        {
            binding.roundCard.setOnClickListener {
                onClick?.invoke(getItem(adapterPosition))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val binding = RoundCardSquareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = rounds.size
    private fun getItem(position: Int): Round = rounds[position]

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val round = getItem(position)
        val result = MatchRepository.getResults(round)

        with(holder.binding) {
            homePlayers.text = round.homePlayers.joinToString(", ") { "${it.name} ${it.surname}" }
            awayPlayers.text = round.awayPlayers.joinToString(", ") { "${it.name} ${it.surname}" }
            sets.text = result.sets
            games.text = result.games
            date.text = round.date.toMyString()
            resolveWinner(holder, round)
        }
    }

    private fun resolveWinner(holder: ViewHolder, round: Round)
    {
        val isHomePlayer = round.homePlayers.find { it.playerId == player.playerId }?.let { true } ?: false
        when
        {
            round.homeSets == null || round.awaySets == null -> setResultButton(holder, "-", R.color.lightGrey)
            round.homeSets!! == round.awaySets!! -> setResultButton(holder, "!", R.color.yellow)
            isHomePlayer && round.homeSets!! > round.awaySets!! -> setResultButton(holder, "V", R.color.blue)
            !isHomePlayer && round.awaySets!! > round.homeSets!! -> setResultButton(holder, "V", R.color.blue)
            else -> setResultButton(holder, "P", R.color.red)
        }
    }

    private fun setResultButton(holder: ViewHolder, text: String, @ColorRes colorRes: Int)
    {
        holder.binding.result.text = text
        holder.binding.result.backgroundTintList = ColorStateList.valueOf(getColor(colorRes))
    }
}