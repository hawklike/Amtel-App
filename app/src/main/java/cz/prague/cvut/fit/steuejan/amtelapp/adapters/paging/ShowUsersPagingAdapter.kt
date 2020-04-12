package cz.prague.cvut.fit.steuejan.amtelapp.adapters.paging

import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.firestore.ktx.toObject
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.getColor
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole.HEAD_OF_LEAGUE
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole.TEAM_MANAGER
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole

class ShowUsersPagingAdapter(options: FirestorePagingOptions<User>, private val currentUser: User?)
    : FirestorePagingAdapter<User, ShowUsersPagingAdapter.ViewHolder>(options)
{
    var onDelete: ((User?) -> Unit)? = null
    var onEdit: ((User?) -> Unit)? = null
    var onClick: ((User?) -> Unit)? = null

    var orderBy = UserOrderBy.SURNAME

    var dataLoadedListener: DataLoadedListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val fullName: TextView = itemView.findViewById(R.id.user_card_name)
        val email: TextView = itemView.findViewById(R.id.user_card_email)
        val birthdate: TextView = itemView.findViewById(R.id.user_card_birthdate)
        val team: TextView = itemView.findViewById(R.id.user_card_team)
        val deleteButton: ImageView = itemView.findViewById(R.id.user_card_delete)
        val editButton: ImageView = itemView.findViewById(R.id.user_card_edit)
        private val card: RelativeLayout = itemView.findViewById(R.id.user_card)

        init
        {
            deleteButton.setOnClickListener {
                onDelete?.invoke(getUser(adapterPosition))
            }

            editButton.setOnClickListener {
                onEdit?.invoke(getUser(adapterPosition))
            }

            card.setOnClickListener {
                onClick?.invoke(getUser(adapterPosition))
            }
        }
    }

    private fun getUser(position: Int): User?
            = getItem(position)?.toObject<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.user_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, user: User)
    {
        if(currentUser?.role?.toRole() == HEAD_OF_LEAGUE)
        {
            with(holder.deleteButton) {
                visibility =
                    if(user.role.toRole() == HEAD_OF_LEAGUE) GONE
                    else VISIBLE
            }
        }
        else
        {
            if(currentUser?.role?.toRole() != TEAM_MANAGER)
                holder.email.visibility = INVISIBLE

            holder.editButton.visibility = GONE
            holder.deleteButton.visibility = GONE
        }

        holder.team.visibility = VISIBLE

        if(user.role.toRole() == HEAD_OF_LEAGUE || user.role.toRole() == TEAM_MANAGER)
            holder.email.visibility = VISIBLE

        holder.fullName.text =
            if(orderBy == UserOrderBy.SURNAME) String.format(context.getString(R.string.full_name_placeholder), user.surname, user.name)
            else String.format(context.getString(R.string.full_name_placeholder), user.name, user.surname)

        holder.email.text = user.email

        holder.birthdate.apply {
            text = user.birthdate?.toMyString() ?: "Datum narození neznámé"
        }

        holder.team.apply {
            text = user.teamName ?: "Bez týmu"
        }

        holder.fullName.apply {
            when
            {
                user.role.toRole() == TEAM_MANAGER -> setTextColor(getColor(R.color.blue))
                user.role.toRole() == HEAD_OF_LEAGUE -> setTextColor(getColor(R.color.yellow))
                else -> setTextColor(getColor(R.color.darkGrey))
            }
        }
    }

    override fun onLoadingStateChanged(state: LoadingState)
    {
        when(state)
        {
            LoadingState.LOADING_INITIAL -> dataLoadedListener?.onLoading()
            LoadingState.LOADING_MORE -> dataLoadedListener?.onLoading()
            LoadingState.LOADED -> dataLoadedListener?.onLoaded()
            LoadingState.FINISHED -> {
                if(itemCount > 7) App.toast("Více už toho není.")
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