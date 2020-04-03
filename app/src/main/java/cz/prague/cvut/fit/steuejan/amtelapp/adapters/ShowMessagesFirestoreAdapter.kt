package cz.prague.cvut.fit.steuejan.amtelapp.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Message


class ShowMessagesFirestoreAdapter(options: FirestoreRecyclerOptions<Message>)
    : FirestoreRecyclerAdapter<Message, ShowMessagesFirestoreAdapter.ViewHolder>(options)
{
    var dataLoadedListener: DataLoadedListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val message: TextView = itemView.findViewById(R.id.message_text)

        init
        {
            message.setOnClickListener {
                toast(getItem(adapterPosition).sentAt?.toMyString(context.getString(R.string.dateTime_format)) ?: "zdravíčko")
            }

            message.setOnLongClickListener {
                copyToClipboard()
                true
            }
        }

        private fun copyToClipboard()
        {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("amtel_chat", getItem(adapterPosition).messageText)
            clipboard.primaryClip = clip
            toast("Zkopírováno")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int
    {
        val message = getItem(position)
        return if(message.fromId == AuthManager.currentUser?.uid) R.layout.message_card_from
        else R.layout.message_card_to
    }

    override fun onDataChanged()
    {
        dataLoadedListener?.onLoaded(itemCount - 1)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, message: Message)
    {
        with(holder.message) {
            text = if(message.fromId == AuthManager.currentUser?.uid) message.messageText
            else
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    Html.fromHtml("<b>${message.fullname}:</b> ${message.messageText}", Html.FROM_HTML_MODE_LEGACY)
                else
                    Html.fromHtml("<b>${message.fullname}:</b> ${message.messageText}")
            }
        }
    }

    interface DataLoadedListener
    {
        fun onLoaded(position: Int)
    }
}
