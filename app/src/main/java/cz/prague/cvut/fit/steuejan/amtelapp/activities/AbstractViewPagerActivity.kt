@file:Suppress("MemberVisibilityCanBePrivate")

package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
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

    override fun onBackPressed()
    {
        super.onBackPressed()
        finish()
    }
}