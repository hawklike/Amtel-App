package cz.prague.cvut.fit.steuejan.amtelapp.business.util

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R

object ColorPicker
{
    fun getColor(position: Int): Int
    {
        return when(position % 5)
        {
            0 -> App.getColor(R.color.blue1)
            1 -> App.getColor(R.color.blue3)
            2 -> App.getColor(R.color.blue4)
            3 -> App.getColor(R.color.blue5)
            4 -> App.getColor(R.color.blue6)
            else -> App.getColor(R.color.blue1)
        }
    }

    fun getDrawable(position: Int): Drawable?
    {
        return when(position % 6)
        {
            0 -> ContextCompat.getDrawable(context, R.drawable.rounded_card1)
            1 -> ContextCompat.getDrawable(context, R.drawable.rounded_card2)
            2 -> ContextCompat.getDrawable(context, R.drawable.rounded_card3)
            3 -> ContextCompat.getDrawable(context, R.drawable.rounded_card4)
            4 -> ContextCompat.getDrawable(context, R.drawable.rounded_card5)
            5 -> ContextCompat.getDrawable(context, R.drawable.rounded_card6)
            else -> null
        }
    }
}