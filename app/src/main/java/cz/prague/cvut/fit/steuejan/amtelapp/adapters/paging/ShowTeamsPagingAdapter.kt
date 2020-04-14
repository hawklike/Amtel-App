package cz.prague.cvut.fit.steuejan.amtelapp.adapters.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.firestore.ktx.toObject
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team

class ShowTeamsPagingAdapter(options: FirestorePagingOptions<Team>)
    : FirestorePagingAdapter<Team, ShowTeamsPagingAdapter.ViewHolder>(options)
{
    var onClick: ((Team?) -> Unit)? = null
    var dataLoadedListener: DataLoadedListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val teamName: TextView = itemView.findViewById(R.id.team_card_name)
        val group: TextView = itemView.findViewById(R.id.team_card_group)
        private val card: RelativeLayout = itemView.findViewById(R.id.team_card)

        init
        {
            card.setOnClickListener {
                onClick?.invoke(getTeam(adapterPosition))
            }
        }
    }

    fun getTeam(position: Int): Team?
            = getItem(position)?.toObject<Team>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.team_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, team: Team)
    {
        holder.teamName.text = team.name
        holder.group.text =
            if(team.groupName == null) "Bez skupiny"
            else team.groupName
    }

    override fun onLoadingStateChanged(state: LoadingState)
    {
        when(state)
        {
            LoadingState.LOADING_INITIAL -> dataLoadedListener?.onLoading()
            LoadingState.LOADING_MORE -> dataLoadedListener?.onLoading()
            LoadingState.LOADED -> dataLoadedListener?.onLoaded()
            LoadingState.FINISHED -> {
                if(itemCount > 15) toast("Více už toho není.")
                dataLoadedListener?.onLoaded()
            }
            LoadingState.ERROR -> {}
        }
    }

    interface DataLoadedListener
    {
        fun onLoaded()
        fun onLoading()
    }
}