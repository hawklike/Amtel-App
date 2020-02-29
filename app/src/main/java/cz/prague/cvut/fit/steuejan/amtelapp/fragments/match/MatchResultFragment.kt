package cz.prague.cvut.fit.steuejan.amtelapp.fragments.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMatchActivityFragment

class MatchResultFragment : AbstractMatchActivityFragment()
{
    private var round = 0

    companion object
    {
        private const val ROUND = "round"

        fun newInstance(round: Int): MatchResultFragment
        {
            val fragment = MatchResultFragment()
            fragment.arguments = Bundle().apply {
                putInt(ROUND, round)
            }
            return fragment
        }
    }

    override fun getName(): String = "MatchResultFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        arguments?.getInt(ROUND)?.let { round = it }
        return inflater.inflate(R.layout.dummy_layout, container, false)
    }
}