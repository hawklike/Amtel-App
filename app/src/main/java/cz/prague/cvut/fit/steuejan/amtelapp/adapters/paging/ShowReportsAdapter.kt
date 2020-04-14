package cz.prague.cvut.fit.steuejan.amtelapp.adapters.paging

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.ktx.toObject
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Report
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.LeagueRepository
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.ReportCardBinding

class ShowReportsAdapter(options: FirestorePagingOptions<Report>)
    : FirestorePagingAdapter<Report, ShowReportsAdapter.ViewHolder>(options)
{
    var onClick: ((report: Report?) -> Unit)? = null
    var onEdit: ((report: Report?) -> Unit)? = null

    inner class ViewHolder(val binding: ReportCardBinding) : RecyclerView.ViewHolder(binding.root)
    {
        init
        {
            binding.card.setOnClickListener {
                onClick?.invoke(getReport(adapterPosition))
            }

            binding.edit.setOnClickListener {
                onEdit?.invoke(getReport(adapterPosition))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val binding = ReportCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, report: Report)
    {
        with(holder.binding) {
            title.text = report.title
            lead.text = report.lead
            date.text = report.date.toMyString()

            if(AuthManager.currentUser?.uid != null && AuthManager.currentUser?.uid == LeagueRepository.headOfLeague?.id)
                edit.visibility = VISIBLE
            else
                edit.visibility = GONE
        }
    }

    private fun getReport(position: Int): Report?
            =  getItem(position)?.toObject<Report>()
}