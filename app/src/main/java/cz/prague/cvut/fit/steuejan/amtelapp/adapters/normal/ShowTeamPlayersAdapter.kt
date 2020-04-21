package cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.getColor
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.adapters.ShowTeamPlayersAdapterVM

class ShowTeamPlayersAdapter(private val context: Context, private val list: MutableList<User>)
    : RecyclerView.Adapter<ShowTeamPlayersAdapter.ViewHolder>()
{
    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(
        ShowTeamPlayersAdapterVM::class.java)

    var onDelete: ((users: MutableList<User>) -> Unit)? = null
    var onClick: ((User) -> Unit)? = null
    var onEdit: ((User) -> Unit)? = null
    var isAllowed = false

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val fullName: TextView = itemView.findViewById(R.id.user_card_name)
        val email: TextView = itemView.findViewById(R.id.user_card_email)
        val birthdate: TextView = itemView.findViewById(R.id.user_card_birthdate)
        val deleteButton: ImageView = itemView.findViewById(R.id.user_card_delete)
        val editButton: ImageView = itemView.findViewById(R.id.user_card_edit)
        private val card: RelativeLayout = itemView.findViewById(R.id.user_card)

        init
        {
            deleteButton.setOnClickListener {
                if(!isAllowed)
                {
                    toast("Po uzavření soupisky nelze hráče smazat.")
                    return@setOnClickListener
                }

                MaterialDialog(context)
                    .title(R.string.delete_user_confirmation_message)
                    .show {
                        positiveButton(text = "Smazat") {
                            viewModel.deleteUser(getItem(adapterPosition))
                            list.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            notifyItemRangeChanged(adapterPosition, list.size)
                            onDelete?.invoke(list)
                        }
                        negativeButton()
                    }
            }

            card.setOnClickListener {
                onClick?.invoke(getItem(adapterPosition))
            }

            editButton.setOnClickListener {
                if(!isAllowed)
                {
                    toast("Po uzavření soupisky nelze hráče upravit.")
                    return@setOnClickListener
                }
                onEdit?.invoke(getItem(adapterPosition))
            }
        }
    }

    fun getItem(position: Int): User = list[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.user_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val user = getItem(position)
        if(user.role.toRole() == UserRole.TEAM_MANAGER)
        {
            holder.deleteButton.visibility = GONE
            holder.fullName.setTextColor(getColor(R.color.blue))
            holder.email.setTextColor(getColor(R.color.blue))
            holder.birthdate.setTextColor(getColor(R.color.blue))
            holder.editButton.visibility = GONE
        }
        else
        {
            holder.deleteButton.visibility = VISIBLE
            holder.fullName.setTextColor(getColor(R.color.darkGrey))
            holder.email.setTextColor(getColor(R.color.darkGrey))
            holder.birthdate.setTextColor(getColor(R.color.darkGrey))
            holder.editButton.visibility = VISIBLE
        }

        holder.fullName.text = String.format(context.getString(R.string.full_name_placeholder), user.surname, user.name)
        holder.email.text = user.email
        user.birthdate?.let { holder.birthdate.text = it.toMyString() }

        if(!isAllowed)
        {
            holder.fullName.alpha = 0.5f
            holder.email.alpha = 0.5f
            holder.birthdate.alpha = 0.5f
            holder.deleteButton.alpha = 0.5f
            holder.editButton.alpha = 0.5f
        }
        else
        {
            holder.fullName.alpha = 1f
            holder.email.alpha = 1f
            holder.birthdate.alpha = 1f
            holder.deleteButton.alpha = 1f
            holder.editButton.alpha = 1f
        }

    }

}