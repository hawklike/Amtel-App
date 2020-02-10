package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group

class ShowGroupsFirestoreAdapter(private val context: Context, options: FirestoreRecyclerOptions<Group>)
    : FirestoreRecyclerAdapter<Group, ShowGroupsFirestoreAdapter.ViewHolder>(options)
{
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val name: TextView = itemView.findViewById(R.id.group_card_name)
        val size: TextView = itemView.findViewById(R.id.group_card_size)
        private val generate: Button = itemView.findViewById(R.id.group_card_generate)

        init
        {
            generate.setOnClickListener {
                Toast.makeText(context, "Tato možnost není zatím funkční.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.group_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, group: Group)
    {
        holder.name.text = group.name
        holder.size.text = "Počet týmů: ${group.teamIds.size}"
    }

}