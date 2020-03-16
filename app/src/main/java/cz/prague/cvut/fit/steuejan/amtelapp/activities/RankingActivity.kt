package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group

class RankingActivity : AbstractViewPagerActivity()
{
    private var group = Group()

    companion object
    {
        const val GROUP = "group"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.group) + " " + group.name)
    }

    override fun getData()
    {
        intent.extras?.let { bundle ->
            group = bundle.getParcelable(GROUP)!!
        }
    }

    override fun setupViewPager(viewPager: ViewPager)
    {
        //TODO
    }
}
