package de.smartsquare.kickdroid.statistics

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.jakewharton.rxbinding2.view.clicks
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickdroid.R
import de.smartsquare.kickdroid.base.BaseActivity
import de.smartsquare.kickdroid.base.DefaultErrorHandler
import de.smartsquare.kickdroid.view.LoadingIndicator
import kotterknife.bindView
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * @author Ruben Gees
 */
class PlayerActivity : BaseActivity() {

    companion object {
        private const val PLAYER_NAME_EXTRA = "player_name"

        fun navigateTo(activity: Activity, playerName: String) {
            activity.startActivity(
                Intent(activity, PlayerActivity::class.java)
                    .putExtra(PLAYER_NAME_EXTRA, playerName)
            )
        }
    }

    private val viewModel by viewModel<PlayerViewModel>()

    private val playerName: String
        get() = intent.getStringExtra(PLAYER_NAME_EXTRA)

    private val toolbar by bindView<Toolbar>(R.id.toolbar)

    private val content by bindView<ViewGroup>(R.id.content)
    private val error by bindView<View>(R.id.error)
    private val loading by bindView<LoadingIndicator>(R.id.loading)

    private val errorText by bindView<TextView>(R.id.errorText)
    private val errorButton by bindView<Button>(R.id.errorButton)

    private val winRate by bindView<TextView>(R.id.winRate)
    private val averageGoals by bindView<TextView>(R.id.averageGoals)
    private val crawls by bindView<TextView>(R.id.averageGoals)
    private val winLossChart by bindView<PieChart>(R.id.winLossChart)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_player)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = playerName

        winLossChart.holeRadius = 0f
        winLossChart.description = null
        winLossChart.transparentCircleRadius = 0f
        winLossChart.legend.isEnabled = false
        winLossChart.setUsePercentValues(false)

        viewModel.statisticSuccess.observe(this, Observer {
            if (it != null) {
                content.visibility = View.VISIBLE
                error.visibility = View.GONE

                winRate.text = it.winRate.toString()
                averageGoals.text = it.averageGoalsPerGame.toString()
                crawls.text = it.totalCrawls.toString()

                val entries = listOf(
                    PieEntry(it.totalWins.toFloat(), getString(R.string.player_wins)),
                    PieEntry(it.totalLosses.toFloat(), getString(R.string.player_losses))
                )

                val dataSet = PieDataSet(entries, null).apply {
                    setColors(intArrayOf(R.color.green, R.color.red), this@PlayerActivity)
                    setDrawValues(false)
                }

                winLossChart.data = PieData(dataSet)
                winLossChart.animateY(resources.getInteger(android.R.integer.config_longAnimTime), Easing.EaseOutQuart)
            }
        })

        viewModel.statisticError.observe(this, Observer {
            if (it != null) {
                content.visibility = View.GONE
                error.visibility = View.VISIBLE

                errorText.text = getString(DefaultErrorHandler.handle(it))
            }
        })

        viewModel.statisticLoading.observe(this, Observer {
            if (it != null) {
                loading.visibility = View.VISIBLE
                content.visibility = View.GONE
                error.visibility = View.GONE
            } else {
                loading.visibility = View.GONE
            }
        })

        errorButton.clicks()
            .autoDisposable(this.scope())
            .subscribe { viewModel.loadStatistic(playerName) }

        if (savedInstanceState == null) {
            viewModel.loadStatistic(playerName)
        }
    }
}
