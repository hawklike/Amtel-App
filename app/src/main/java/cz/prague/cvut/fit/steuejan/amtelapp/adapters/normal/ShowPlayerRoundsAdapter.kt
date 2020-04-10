package cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.getColor
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Player
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.RoundCardSquareBinding

class ShowPlayerRoundsAdapter(private val rounds: List<Round>, private val player: Player)
    : RecyclerView.Adapter<ShowPlayerRoundsAdapter.ViewHolder>()
{
    inner class ViewHolder(val binding: RoundCardSquareBinding) : RecyclerView.ViewHolder(binding.root)
    {
        init
        {
            binding.roundCard.setOnClickListener {

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
        val result = MatchManager.getResults(round)

        with(holder) {
            binding.homePlayers.text = round.homePlayers.joinToString(", ") { "${it.name} ${it.surname}" }
            binding.awayPlayers.text = round.awayPlayers.joinToString(", ") { "${it.name} ${it.surname}" }
            binding.sets.text = result.sets
            binding.games.text = result.games
            binding.date.text = round.date.toMyString()
            resolveWinner(this, round)
        }
    }

    private fun resolveWinner(holder: ViewHolder, round: Round)
    {
        val isHomePlayer = round.homePlayers.find { it.playerId == player.playerId }?.let { true } ?: false
        when
        {
            round.homeGems == null || round.awayGems == null -> setResultButton(holder, "-", R.color.lightGrey)
            round.homeGems!! == round.awayGems!! -> setResultButton(holder, "-", R.color.lightGrey)
            isHomePlayer && round.homeGems!! > round.awayGems!! -> setResultButton(holder, "V", R.color.blue)
            !isHomePlayer && round.awayGems!! > round.homeGems!! -> setResultButton(holder, "V", R.color.blue)
            else -> setResultButton(holder, "P", R.color.red)
        }
    }

    private fun setResultButton(holder: ViewHolder, text: String, @ColorRes colorRes: Int)
    {
        holder.binding.result.text = text
        holder.binding.result.backgroundTintList = ColorStateList.valueOf(getColor(colorRes))
    }
}