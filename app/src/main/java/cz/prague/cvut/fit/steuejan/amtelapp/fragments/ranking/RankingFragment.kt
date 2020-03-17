package cz.prague.cvut.fit.steuejan.amtelapp.fragments.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractBaseFragment

class RankingFragment : AbstractBaseFragment()
{
    private var year = 0
    private var group = Group()

    companion object
    {
        private const val GROUP = "group"
        private const val YEAR = "year"

        fun newInstance(group: Group, year: Int): RankingFragment
        {
            val fragment = RankingFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(GROUP, group)
                putInt(YEAR, year)
            }
            return fragment
        }
    }

    override fun getName(): String = "RankingFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        arguments?.getParcelable<Group>(GROUP)?.let { group = it }
        arguments?.getInt(YEAR)?.let { year = it }
        return inflater.inflate(R.layout.ranking_fragment, container, false)
    }

}