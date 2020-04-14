package cz.prague.cvut.fit.steuejan.amtelapp.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText

class AnchoredEditText : AppCompatEditText
{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun dispatchTouchEvent(event: MotionEvent): Boolean
    {
        when(event.action)
        {
            MotionEvent.ACTION_DOWN ->
                this.parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP ->
                this.parent.requestDisallowInterceptTouchEvent(false)
        }
        super.dispatchTouchEvent(event)
        return true
    }
}