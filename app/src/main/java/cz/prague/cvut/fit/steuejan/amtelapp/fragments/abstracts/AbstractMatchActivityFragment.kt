package cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts

import androidx.fragment.app.activityViewModels
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities.MatchViewPagerActivityVM

abstract class AbstractMatchActivityFragment : AbstractBaseFragment()
{
    protected val matchViewModel by activityViewModels<MatchViewPagerActivityVM>()
}