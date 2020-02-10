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
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.TeamsAdapterVM

class ShowTeamsFirestoreAdapter(private val context: Context, options: FirestoreRecyclerOptions<Team>)
    : FirestoreRecyclerAdapter<Team, ShowTeamsFirestoreAdapter.ViewHolder>(options)
{
    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(TeamsAdapterVM::class.java)

    @Suppress("MemberVisibilityCanBePrivate")
    var simple: Boolean = true
    var groups: List<String> = listOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val teamName: TextView = itemView.findViewById(R.id.team_card_name)
        val group: TextView = itemView.findViewById(R.id.team_card_group)
        private val editButton: ImageView = itemView.findViewById(R.id.team_card_add)

        init
        {
            editButton.setOnClickListener {
                val team = getItem(adapterPosition)

                val index = team.group?.let {
                     groups.indexOf(it)
                } ?: -1

                MaterialDialog(context).show {
                    title(R.string.choose_group)
                    listItemsSingleChoice(items = groups, initialSelection = index) { _, _, item ->
                        viewModel.addToGroup(team, item.toString())
                    }
                    positiveButton(R.string.ok)
                }
            }
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
            team.group?.let { holder.group.text = it }
        }
    }

}