package cz.prague.cvut.fit.steuejan.amtelapp.fragments.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMatchActivityFragment

class MatchInputResultFragment : AbstractMatchActivityFragment()
{
    companion object
    {
        fun newInstance(): MatchInputResultFragment = MatchInputResultFragment()
    }

    override fun getName(): String = "MatchInputResultFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.dummy_layout, container, false)
    }

}