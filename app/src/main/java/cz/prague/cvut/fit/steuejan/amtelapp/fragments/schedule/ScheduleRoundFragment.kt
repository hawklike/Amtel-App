package cz.prague.cvut.fit.steuejan.amtelapp.fragments.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.AbstractBaseFragment

class ScheduleRoundFragment : AbstractBaseFragment()
{
    private lateinit var group: Group

    companion object
    {
        const val GROUP = "group"

        fun newInstance(group: Group): ScheduleRoundFragment
        {
            val fragment = ScheduleRoundFragment()
            fragment.arguments = Bundle().apply { putParcelable(GROUP, group) }
            return fragment
        }
    }

    override fun setProgressBar(on: Boolean) {}

    override fun getName(): String = "ScheduleRoundFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        group = arguments?.getParcelable(GROUP) ?: Group()
        return inflater.inflate(R.layout.dummy_layout, container, false)
    }
}