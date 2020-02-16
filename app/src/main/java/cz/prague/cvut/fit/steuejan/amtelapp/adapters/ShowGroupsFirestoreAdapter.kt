package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ShowGroupsFirestoreAdapterVM

class ShowGroupsFirestoreAdapter(private val context: Context, options: FirestoreRecyclerOptions<Group>)
    : FirestoreRecyclerAdapter<Group, ShowGroupsFirestoreAdapter.ViewHolder>(options)
{
    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(
        ShowGroupsFirestoreAdapterVM::class.java)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val name: TextView = itemView.findViewById(R.id.group_card_name)
        val size: TextView = itemView.findViewById(R.id.group_card_size)
        private val generate: Button = itemView.findViewById(R.id.group_card_generate)

        var rounds = 0
        var calculatedRounds = 0

        init
        {
            generate.setOnClickListener {
                MaterialDialog(context).show {
                    title(text = "Vygenerovat plán pro skupinu ${name.text}?")
                    input(
                        waitForPositiveButton = false,
                        hint = "Počet kol",
                        prefill = rounds.toString(),
                        inputType = InputType.TYPE_CLASS_NUMBER) { dialog, text ->

                        val isValid = viewModel.confirmInput(text.toString(), calculatedRounds)
                        if(isValid) rounds = text.toString().toInt()
                        if(rounds == 0) dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                        else
                        {
                            dialog.getInputField().error = if(isValid) null
                            else App.context.getString(R.string.generate_group_error_text) + " " + calculatedRounds + context.getString(R.string.dot)
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                        }
                    }
                    positiveButton(R.string.generate_plan) {
                        viewModel.generateMatches(name.text.toString(), rounds)
                    }
                }

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

        val size = group.teamIds.size
        holder.size.text = "Počet týmů: $size"
        holder.rounds = if(size % 2 == 0) (size - 1) else size
        if(size == 1 || size == 0) holder.rounds = 0
        holder.calculatedRounds = holder.rounds
    }

}