@file:Suppress("MemberVisibilityCanBePrivate")

package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R


abstract class AbstractViewPagerActivity : AbstractBaseActivity()
{
    protected lateinit var viewPager: ViewPager
    protected lateinit var tabs: TabLayout

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.activity_viewpager)
        super.onCreate(savedInstanceState)
        setArrowBack()

        viewPager = findViewById(R.id.activity_viewpager_menu_viewPager)
        tabs = findViewById(R.id.activity_viewpager_menu_tabs)

        getData()
        setupViewPager(viewPager)
        tabs.setupWithViewPager(viewPager)
    }

    protected abstract fun getData()
    protected abstract fun setupViewPager(viewPager: ViewPager)

    /*
    Animates transition to a new page with a custom duration.
    Solution found: https://stackoverflow.com/a/30976853/9723204
     */
    protected fun animatePagerTransition(forward: Boolean = true)
    {
        val animator = ValueAnimator.ofInt(0, viewPager.width)
        animator.addListener(object : Animator.AnimatorListener
        {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator)
            {
                viewPager.endFakeDrag()
            }

            override fun onAnimationCancel(animation: Animator)
            {
                viewPager.endFakeDrag()
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })

        animator.interpolator = AccelerateInterpolator()

        animator.addUpdateListener(object : AnimatorUpdateListener
        {
            private var oldDragPosition = 0
            override fun onAnimationUpdate(animation: ValueAnimator)
            {
                val dragPosition = animation.animatedValue as Int
                val dragOffset = dragPosition - oldDragPosition
                oldDragPosition = dragPosition
                viewPager.fakeDragBy(dragOffset * (if(forward) -1 else 1).toFloat())
            }
        })

        animator.duration = 400
        if(viewPager.beginFakeDrag()) animator.start()
    }
}