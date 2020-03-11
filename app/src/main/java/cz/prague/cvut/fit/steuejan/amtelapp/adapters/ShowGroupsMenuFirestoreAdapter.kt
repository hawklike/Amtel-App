package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.ColorPicker
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group

class ShowGroupsMenuFirestoreAdapter(options: FirestoreRecyclerOptions<Group>)
    : FirestoreRecyclerAdapter<Group, ShowGroupsMenuFirestoreAdapter.ViewHolder>(options)
{
    private var isNextVisible = false
    private var onNextClick: (group: Group) -> Unit = {}

    fun setNextButton(isVisible: Boolean, onClick: ((group: Group) -> Unit)? = null)
    {
        isNextVisible = isVisible
        onNextClick = onClick?.let { it }
            ?: { toast(R.string.not_working_yet) }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val card: RelativeLayout = itemView.findViewById(R.id.group_menu_card)
        val name: TextView = itemView.findViewById(R.id.group_menu_card_name)
        val logo: TextView = itemView.findViewById(R.id.group_menu_card_logo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.group_menu_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, group: Group)
    {
        holder.card.background = ColorPicker.getDrawable(position)
        holder.name.text = group.name
        holder.logo.text = group.name.fold(StringBuilder()) { acc, c ->
            if(acc.length > 2) return@fold acc
            acc.append(c)
        }

        val rounds = group.rounds[DateUtil.actualYear.toString()]
        if(rounds == 0 || rounds == null)
            holder.card.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.veryLightGrey))
    }

}