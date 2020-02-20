package cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts

import androidx.fragment.app.activityViewModels
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.ScheduleRoundsActivityVM

abstract class InsideScheduleActivityFragment : AbstractBaseFragment()
{
    protected val scheduleViewModel by activityViewModels<ScheduleRoundsActivityVM>()
}