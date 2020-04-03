package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.UsersAdapterVM

//TODO: change to pagination adapter
class ShowUsersFirestoreAdapter(private val context: Context, options: FirestoreRecyclerOptions<User>)
    : FirestoreRecyclerAdapter<User, ShowUsersFirestoreAdapter.ViewHolder>(options)
{
    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(UsersAdapterVM::class.java)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val fullName: TextView = itemView.findViewById(R.id.user_card_name)
        val email: TextView = itemView.findViewById(R.id.user_card_email)
        val birthdate: TextView = itemView.findViewById(R.id.user_card_birthdate)
        val team: TextView = itemView.findViewById(R.id.user_card_team)
        val deleteButton: ImageView = itemView.findViewById(R.id.user_card_delete)
        val editButton: ImageView = itemView.findViewById(R.id.user_card_edit)

        init
        {
            deleteButton.setOnClickListener {

                MaterialDialog(context)
                    .title(R.string.delete_user_confirmation_message)
                    .show {
                        positiveButton(R.string.yes) {
                            viewModel.deleteUser(getItem(adapterPosition))
                        }
                        negativeButton(R.string.no)
                    }
            }

            editButton.setOnClickListener {
                Toast.makeText(context, context.getString(R.string.not_working_yet), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.user_card, parent, false)
        return ViewHolder(view)
    }

    //FIXME: sometimes wrong data passed in
    override fun onBindViewHolder(holder: ViewHolder, position: Int, user: User)
    {
        if(user.role.toRole() == UserRole.HEAD_OF_LEAGUE)
            holder.deleteButton.visibility = View.GONE
        holder.team.visibility = View.VISIBLE
        holder.editButton.visibility = View.VISIBLE

        holder.fullName.text = String.format(context.getString(R.string.full_name_placeholder), user.surname, user.name)
        if(user.role.toRole() == UserRole.TEAM_MANAGER)
            holder.fullName.setTextColor(App.getColor(R.color.blue))

        holder.email.text = user.email
        user.birthdate?.let { holder.birthdate.text = it.toMyString() }
        user.teamName?.let { holder.team.text = it }
    }

}