package cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts

import androidx.fragment.app.activityViewModels
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MatchMenuActivityVM

abstract class AbstractMatchActivityFragment : AbstractBaseFragment()
{
    protected val matchViewModel by activityViewModels<MatchMenuActivityVM>()
}