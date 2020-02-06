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
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MakeTeamUsersAdapterVM

class MakeTeamUsersAdapter(context: Context, private val list: MutableList<User>) : RecyclerView.Adapter<MakeTeamUsersAdapter.ViewHolder>()
{
    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(MakeTeamUsersAdapterVM::class.java)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val fullName = itemView.findViewById<TextView>(R.id.user_card_name)!!
        val email = itemView.findViewById<TextView>(R.id.user_card_email)!!
        val birthdate = itemView.findViewById<TextView>(R.id.user_card_birthdate)!!
        private val deleteButton = itemView.findViewById<ImageView>(R.id.user_card_delete)!!

        init
        {
            deleteButton.setOnClickListener {
                viewModel.deleteUser(getItem(adapterPosition))
                list.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                notifyItemRangeChanged(adapterPosition, list.size)
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

        @SuppressLint("SetTextI18n")
        holder.fullName.text = "${user.name} ${user.surname}"
        holder.email.text = user.email
        user.birthdate?.let { holder.birthdate.text = DateUtil.toString(it) }
    }

}