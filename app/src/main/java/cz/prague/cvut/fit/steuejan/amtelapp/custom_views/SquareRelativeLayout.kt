package cz.prague.cvut.fit.steuejan.amtelapp.custom_views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

/** A RelativeLayout that will always be square -- same width and height,
 * where the height is based off the width.
 *
 */
class SquareRelativeLayout : RelativeLayout
{
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}