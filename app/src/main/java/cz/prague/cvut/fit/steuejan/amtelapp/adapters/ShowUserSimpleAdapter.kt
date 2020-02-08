package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.UsersAdapterVM

class ShowUserSimpleAdapter(private val context: Context, private val list: MutableList<User>) : RecyclerView.Adapter<ShowUserSimpleAdapter.ViewHolder>()
{
    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(UsersAdapterVM::class.java)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val fullName: TextView = itemView.findViewById(R.id.user_card_name)
        val email: TextView = itemView.findViewById(R.id.user_card_email)
        val birthdate: TextView = itemView.findViewById(R.id.user_card_birthdate)
        val deleteButton: ImageView = itemView.findViewById(R.id.user_card_delete)

        init
        {
            deleteButton.setOnClickListener {

                MaterialDialog(context)
                    .title(R.string.delete_user_confirmation_message)
                    .show {
                        positiveButton(R.string.yes) {
                            viewModel.deleteUser(getItem(adapterPosition))
                            list.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            notifyItemRangeChanged(adapterPosition, list.size)
                        }
                        negativeButton(R.string.no)
                    }
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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val user = getItem(position)
        if(UserRole.toRole(user.role) == UserRole.TEAM_MANAGER) holder.deleteButton.visibility = View.GONE

        holder.fullName.text = "${user.name} ${user.surname}"
        holder.email.text = user.email
        user.birthdate?.let { holder.birthdate.text = DateUtil.toString(it) }
    }

}