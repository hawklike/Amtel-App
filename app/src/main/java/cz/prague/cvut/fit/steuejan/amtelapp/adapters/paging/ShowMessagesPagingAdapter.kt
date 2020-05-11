package cz.prague.cvut.fit.steuejan.amtelapp.adapters.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Message

class ShowMessagesPagingAdapter(options: FirestorePagingOptions<Message>)
    : FirestorePagingAdapter<Message, ShowMessagesPagingAdapter.ViewHolder>(options)
{
    var dataLoadedListener: DataLoadedListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val messageText: TextView = itemView.findViewById(R.id.message_card_universal_text)
        val messageDate: TextView = itemView.findViewById(R.id.message_card_universal_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.message_card_universal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, message: Message)
    {
        holder.messageText.text = message.messageText
        holder.messageDate.text = message.sentAt?.toMyString(context.getString(R.string.dateTime_format))
    }

    override fun onLoadingStateChanged(state: LoadingState)
    {
        when(state)
        {
            LoadingState.LOADING_INITIAL -> dataLoadedListener?.onLoading()
            LoadingState.LOADING_MORE -> dataLoadedListener?.onLoading()
            LoadingState.LOADED -> dataLoadedListener?.onLoaded()
            LoadingState.FINISHED -> {
                if(itemCount > 12) toast(R.string.everything_loaded)
                dataLoadedListener?.onLoaded()
            }
            LoadingState.ERROR -> {}
        }
    }

    interface DataLoadedListener
    {
        fun onLoaded()
        fun onLoading()
    }

}