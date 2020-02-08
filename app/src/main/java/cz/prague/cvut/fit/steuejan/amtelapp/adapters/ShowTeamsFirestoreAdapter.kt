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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.TeamsAdapterVM

class ShowTeamsFirestoreAdapter(private val context: Context, options: FirestoreRecyclerOptions<Team>, private val simple: Boolean)
    : FirestoreRecyclerAdapter<Team, ShowTeamsFirestoreAdapter.ViewHolder>(options)
{
    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(TeamsAdapterVM::class.java)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val teamName: TextView = itemView.findViewById(R.id.team_card_name)
        val group: TextView = itemView.findViewById(R.id.team_card_group)
        val editButton: ImageView = itemView.findViewById(R.id.team_card_add)

        init
        {
//            deleteButton.setOnClickListener {
//
//                MaterialDialog(context)
//                    .title(R.string.delete_user_confirmation_message)
//                    .show {
//                        positiveButton(R.string.yes) {
////                            viewModel.deleteUser(getItem(adapterPosition))
//                        }
//                        negativeButton(R.string.no)
//                    }
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.team_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, team: Team)
    {
        if(simple)
        {
            holder.teamName.text = team.name
            team.group?.let { holder.teamName.text = it }
        }
    }

}