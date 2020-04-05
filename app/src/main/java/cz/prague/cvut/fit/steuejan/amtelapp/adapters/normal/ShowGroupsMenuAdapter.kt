package cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.StringUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ShowGroupsMenuAdapterVM

class ShowGroupsMenuAdapter(context: Context, private val list: List<Group>, private val isRanking: Boolean)
    : RecyclerView.Adapter<ShowGroupsMenuAdapter.ViewHolder>()
{
    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(
        ShowGroupsMenuAdapterVM::class.java)

    var onNextClick: ((group: Group, actualRound: Int) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val card: RelativeLayout = itemView.findViewById(R.id.group_menu_card)
        val name: TextView = itemView.findViewById(R.id.group_menu_card_name)
        val logo: TextView = itemView.findViewById(R.id.group_menu_card_logo)
        val rounds: TextView = itemView.findViewById(R.id.group_menu_card_size)
        val actualRound: TextView = itemView.findViewById(R.id.group_menu_card_actual)

        var actualRoundInt: Int? = null

        init
        {
            card.setOnClickListener {
                onNextClick?.invoke(getItem(adapterPosition), actualRoundInt?.minus(1) ?: 0)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.group_menu_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val group = getItem(position)
        holder.name.text = group.name
        holder.logo.text = StringUtil.createLabel(group.name)

        if(isRanking)
        {
            viewModel.isRanking(holder, group)
            return
        }

        if(group.playOff)
        {
            viewModel.getPlayOffDate(holder, group)
            return
        }

        val rounds = group.rounds[DateUtil.actualSeason]
        if(rounds == 0 || rounds == null)
        {
            viewModel.disableCard(holder)
            holder.rounds.text = String.format(context.getString(R.string.rounds_number_input), "0")
            holder.actualRound.text = String.format(context.getString(R.string.actual_round), context.getString(R.string.is_not))
        }
        else
        {
            holder.rounds.text = String.format(context.getString(R.string.rounds_number_input), rounds)
            holder.actualRoundInt = viewModel.getActualRound(group).also { round ->
                holder.actualRound.text =
                    round?.let { String.format(context.getString(R.string.actual_round), it) }
                        ?: String.format(context.getString(R.string.actual_round), context.getString(R.string.is_not))
            }
        }
    }

    override fun getItemCount(): Int = list.size
    private fun getItem(position: Int): Group = list[position]

}
