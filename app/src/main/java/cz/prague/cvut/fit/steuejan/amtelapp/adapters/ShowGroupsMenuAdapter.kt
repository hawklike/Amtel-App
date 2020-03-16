package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ShowGroupsMenuFirestoreAdapterVM

class ShowGroupsMenuAdapter(context: Context, private val list: List<Group>, private val isRanking: Boolean)
    : RecyclerView.Adapter<ShowGroupsMenuAdapter.ViewHolder>()
{
    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(
        ShowGroupsMenuFirestoreAdapterVM::class.java)

    var onNextClick: (group: Group, actualRound: Int) -> Unit = { _, _ -> toast(R.string.not_working_yet) }

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
                onNextClick.invoke(getItem(adapterPosition), actualRoundInt?.minus(1) ?: 0)
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
        holder.logo.text = viewModel.createLabel(group)

        if(isRanking)
        {
            holder.actualRound.text = String.format(context.getString(R.string.last_active_season),
                group.teamIds.keys.map { it.toInt() }.max() ?: context.getString(R.string.is_not))
            holder.rounds.text = String.format(context.getString(R.string.teams_number_this_year),
                group.teamIds[DateUtil.actualYear]?.size ?: 0)
            if(group.teamIds.isEmpty()) disableCard(holder)
            return
        }

        val rounds = group.rounds[DateUtil.actualYear]
        if(rounds == 0 || rounds == null)
        {
            disableCard(holder)
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

    private fun disableCard(holder: ViewHolder)
    {
        holder.card.isEnabled = false
        holder.card.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.veryLightGrey))
        holder.card.elevation = 0.0F
        holder.name.setTextColor(App.getColor(R.color.lightGrey))
        holder.rounds.setTextColor(App.getColor(R.color.lightGrey))
        holder.logo.setTextColor(App.getColor(R.color.lightGrey))
        holder.actualRound.setTextColor(App.getColor(R.color.lightGrey))
    }

    override fun getItemCount(): Int = list.size
    private fun getItem(position: Int): Group = list[position]

}
