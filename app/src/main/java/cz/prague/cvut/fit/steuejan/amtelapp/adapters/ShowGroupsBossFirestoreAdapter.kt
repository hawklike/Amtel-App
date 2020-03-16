package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.content.Context
import android.graphics.Color
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
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ShowGroupsFirestoreAdapterVM

class ShowGroupsBossFirestoreAdapter(private val context: Context, options: FirestoreRecyclerOptions<Group>)
    : FirestoreRecyclerAdapter<Group, ShowGroupsBossFirestoreAdapter.ViewHolder>(options)
{
    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(
        ShowGroupsFirestoreAdapterVM::class.java)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var group: Group

        private val name: TextView = itemView.findViewById(R.id.group_card_name)
        private val size: TextView = itemView.findViewById(R.id.group_card_size)
        private val generate: Button = itemView.findViewById(R.id.group_card_generate)

        private var rounds = 0
        private var calculatedRounds = 0

        fun init(group: Group)
        {
            this.group = group
            val size = group.teamIds[DateUtil.actualYear]?.size ?: 0

            name.text = group.name
            this.size.text = String.format(context.getString(R.string.number_teams), size)

            rounds = if(size % 2 == 0) (size - 1) else size
            if(size == 1 || size == 0) rounds = 0
            calculatedRounds = rounds

            generate()
        }

        //TODO: implement regenerating matches
        private fun generate()
        {
            if(rounds == 0)
            {
                generate.isEnabled = false
                generate.setTextColor(App.getColor(R.color.lightGrey))
            }
            else
            {
                val rounds = group.rounds[DateUtil.actualYear]
                if(rounds != null && rounds != 0)
                {
                    generate.text = context.getString(R.string.regenerate_matches)
                    generate.setTextColor(Color.RED)
                }
            }
            showDialog()
        }

        private fun showDialog()
        {
            generate.setOnClickListener {
                MaterialDialog(context).show {
                    title(text = context.getString(R.string.generate_matches_dialog_title) + " ${name.text}")
                    input(
                        waitForPositiveButton = false,
                        hint = context.getString(R.string.rounds_number),
                        prefill = rounds.toString(),
                        inputType = InputType.TYPE_CLASS_NUMBER) { dialog, text ->

                        val isValid = confirmInput(text)
                        if(rounds == 0) dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                        else
                        {
                            dialog.getInputField().error = if(isValid) null
                            else String.format(context.getString(R.string.generate_group_error_text), calculatedRounds)
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                        }
                    }
                    positiveButton(R.string.generate_plan) { generateSchedule() }
                }
            }
        }

        private fun confirmInput(text: CharSequence): Boolean
        {
            val isValid = viewModel.confirmInput(text.toString(), calculatedRounds)
            if(isValid) rounds = text.toString().toInt()
            return isValid
        }

        private fun generateSchedule()
        {
            viewModel.generateMatches(getItem(adapterPosition), rounds)
            generate.setTextColor(App.getColor(R.color.red))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.group_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, group: Group)
    {
        holder.init(group)
    }

}