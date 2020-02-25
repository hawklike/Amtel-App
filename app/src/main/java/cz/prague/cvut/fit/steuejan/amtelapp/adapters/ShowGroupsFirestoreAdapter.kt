package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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

    private var isNextVisible = false
    private var onNextClick: (group: Group) -> Unit = {}

    fun setNextButton(isVisible: Boolean, onClick: ((group: Group) -> Unit)? = null)
    {
        isNextVisible = isVisible
        onNextClick = onClick?.let { it }
            ?: { Toast.makeText(context, context.getString(R.string.not_working_yet), Toast.LENGTH_SHORT).show() }
    }

    @SuppressLint("SetTextI18n")
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var group: Group

        private val name: TextView = itemView.findViewById(R.id.group_card_name)
        private val size: TextView = itemView.findViewById(R.id.group_card_size)
        private val generate: Button = itemView.findViewById(R.id.group_card_generate)
        private val next: ImageButton = itemView.findViewById(R.id.group_card_next)

        private var rounds = 0
        private var calculatedRounds = 0

        fun init(group: Group)
        {
            this.group = group
            val size = group.teamIds.size

            name.text = group.name
            this.size.text = "Počet týmů: $size"

            rounds = if(size % 2 == 0) (size - 1) else size
            if(size == 1 || size == 0) rounds = 0
            calculatedRounds = rounds

            generate()
            next()
        }

        private fun generate()
        {
            if(rounds == 0)
            {
                generate.isEnabled = false
                generate.setTextColor(ContextCompat.getColor(App.context, R.color.lightGrey))
            }
            else if(group.rounds != 0) generate.setTextColor(Color.RED)

            generate.setOnClickListener {
                MaterialDialog(context).show {
                    title(text = "Vygenerovat plán pro skupinu ${name.text}?")
                    input(
                        waitForPositiveButton = false,
                        hint = context.getString(R.string.rounds_number),
                        prefill = rounds.toString(),
                        inputType = InputType.TYPE_CLASS_NUMBER) { dialog, text ->

                        val isValid = viewModel.confirmInput(text.toString(), calculatedRounds)
                        if(isValid) rounds = text.toString().toInt()
                        if(rounds == 0) dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                        else
                        {
                            dialog.getInputField().error = if(isValid) null
                            else context.getString(R.string.generate_group_error_text) + " " + calculatedRounds + "."
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                        }
                    }
                    positiveButton(R.string.generate_plan) {
                        viewModel.generateMatches(name.text.toString(), rounds)
                        generate.setTextColor(Color.RED)
                    }
                }
            }
        }

        private fun next()
        {
            if(isNextVisible)
            {
                if(group.rounds == 0) next.visibility = View.GONE
                else next.visibility = View.VISIBLE

                generate.visibility = View.GONE
                next.setOnClickListener {
                    onNextClick.invoke(group)
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int, group: Group)
    {
        holder.init(group)
    }

}