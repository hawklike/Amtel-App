package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.callbacks.ItemMoveCallback
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.services.GenerateScheduleService
import cz.prague.cvut.fit.steuejan.amtelapp.services.GroupDeletionService
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ShowGroupsBossAdapterVM
import java.util.*

class ShowGroupsBossAdapter(private val context: Context, val list: MutableList<Group>)
    : RecyclerView.Adapter<ShowGroupsBossAdapter.ViewHolder>(), ItemMoveCallback.ItemTouchHelperContract
{
    private val viewModel = ViewModelProviders.of(context as FragmentActivity).get(
        ShowGroupsBossAdapterVM::class.java)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var group: Group

        private val name: TextView = itemView.findViewById(R.id.group_card_name)
        private val size: TextView = itemView.findViewById(R.id.group_card_size)
        private val playingPlayOff: TextView = itemView.findViewById(R.id.group_card_playingPlayOff)
        private val generateButton: Button = itemView.findViewById(R.id.group_card_generate)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.group_card_delete)
        val visibility: ImageButton = itemView.findViewById(R.id.group_card_visibility)

        private var rounds = 0
        private var calculatedRounds = 0

        fun init(group: Group)
        {
            this.group = group

            val size = group.teamIds[DateUtil.actualSeason]?.size ?: 0

            if(group.playingPlayOff) playingPlayOff.text = "Hraje baráž: ano"
            else playingPlayOff.text = "Hraje baráž: ne"

            name.text = group.name
            this.size.text = String.format(context.getString(R.string.number_teams), size)

            rounds = if(size % 2 == 0) (size - 1) else size
            if(size == 1 || size == 0) rounds = 0
            calculatedRounds = rounds

            handleGenerateButton()
            handleDeleteButton()
            handleVisibility()
        }

        private fun handleVisibility()
        {
            if(!group.visibility) visibility.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.lightGrey))
            visibility.setOnClickListener {
                val option = if(group.visibility) "vypnout" else "zapnout"
                MaterialDialog(context)
                    .title(text = "Chcete $option viditelnost skupiny ${group.name}?")
                    .show {
                        positiveButton(R.string.yes) {
                            viewModel.handleVisibility(!group.visibility, getItem(adapterPosition), this@ViewHolder)
                        }
                        negativeButton()
                    }
            }
        }

        private fun handleDeleteButton()
        {
            deleteButton.setOnClickListener {

                MaterialDialog(context)
                    .title(text = "Opravdu chcete smazat skupinu ${name.text}?")
                    .show {
                        positiveButton(R.string.yes) {
                            deleteGroup(getItem(adapterPosition))
                            list.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            notifyItemRangeChanged(adapterPosition, list.size)
                        }
                        negativeButton()
                    }
            }
        }

        private fun deleteGroup(group: Group)
        {
            val serviceIntent = Intent(context, GroupDeletionService::class.java).apply {
                putExtra(GroupDeletionService.GROUP, group)
            }
            context.startService(serviceIntent)
        }

        private fun handleGenerateButton()
        {
            if(rounds == 0)
            {
                generateButton.isEnabled = false
                generateButton.setTextColor(App.getColor(R.color.lightGrey))
            }
            else
            {
                val rounds = group.rounds[DateUtil.actualSeason]
                if(rounds != null && rounds != 0)
                {
                    generateButton.text = context.getString(R.string.regenerate_matches)
                    generateButton.setTextColor(Color.RED)
                    showDialog(
                        title = "Přegenerovat utkání ve skupině ${name.text}?",
                        buttonText = "Přegenerovat utkání",
                        message = "Veškerá utkání v této skupině a tento rok budou nenavrátně přepsána.\n\nZadejte počet kol:") {
                        generateSchedule(true)
                    }

                    return
                }
            }
            showDialog(
                title = "Vygenerovat utkání ve skupině ${name.text}?",
                message = "Zadejte počet kol:",
                buttonText = context.getString(R.string.generate_plan)) {
                generateSchedule(false)
            }
        }

        private fun showDialog(title: String, buttonText: String, message: String, callback: () -> Unit)
        {
            generateButton.setOnClickListener {
                MaterialDialog(context).show {
                    title(text = title)
                    message(text = message)
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
                    positiveButton(text = buttonText) { callback.invoke() }
                    negativeButton()
                }
            }
        }

        private fun confirmInput(text: CharSequence): Boolean
        {
            val isValid = viewModel.confirmInput(text.toString(), calculatedRounds)
            if(isValid) rounds = text.toString().toInt()
            return isValid
        }

        private fun generateSchedule(regenerate: Boolean)
        {
            val serviceIntent = Intent(context, GenerateScheduleService::class.java).apply {
                putExtra(GenerateScheduleService.GROUP, getItem(adapterPosition))
                putExtra(GenerateScheduleService.ROUNDS, rounds)
                putExtra(GenerateScheduleService.REGENERATE, regenerate)

            }
            context.startService(serviceIntent)

            generateButton.setTextColor(App.getColor(R.color.red))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.group_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val group = getItem(position)
        holder.init(group)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int)
    {
        if(fromPosition < toPosition)
        {
            for(i in fromPosition until toPosition)
                Collections.swap(list, i, i + 1)
        }
        else
        {
            for(i in fromPosition downTo toPosition + 1)
                Collections.swap(list, i, i - 1)
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun getItemCount(): Int = list.size
    private fun getItem(position: Int): Group = list[position]

}