package de.smartsquare.kickdroid.statistics

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import de.smartsquare.kickdroid.R
import de.smartsquare.kickdroid.base.BaseActivity
import kotterknife.bindView

/**
 * @author Ruben Gees
 */
class StatisticsActivity : BaseActivity() {

    companion object {
        fun navigateTo(activity: Activity) {
            activity.startActivity(Intent(activity, StatisticsActivity::class.java))
        }
    }

    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val tabs by bindView<TabLayout>(R.id.tabs)
    private val viewPager by bindView<ViewPager>(R.id.viewPager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_statistics)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewPager.adapter = StatisticsAdapter(supportFragmentManager)
        tabs.setupWithViewPager(viewPager)
    }

    override fun onDestroy() {
        viewPager.adapter = null

        super.onDestroy()
    }

    private inner class StatisticsAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> StatisticsFragment.newInstance(StatisticsType.SOLO)
                1 -> StatisticsFragment.newInstance(StatisticsType.DUO)
                2 -> StatisticsFragment.newInstance(StatisticsType.FLEX)
                else -> throw IllegalArgumentException("Unknown position: $position")
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> getString(R.string.statistics_solo_title)
                1 -> getString(R.string.statistics_duo_title)
                2 -> getString(R.string.statistics_flex_title)
                else -> throw IllegalArgumentException("Unknown position: $position")
            }
        }

        override fun getCount(): Int {
            return 3
        }
    }
}
