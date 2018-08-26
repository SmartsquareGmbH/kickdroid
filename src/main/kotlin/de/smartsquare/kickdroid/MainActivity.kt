package de.smartsquare.kickdroid

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.mikepenz.iconics.view.IconicsImageView
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickdroid.base.BaseActivity
import de.smartsquare.kickdroid.base.activityInject
import de.smartsquare.kickdroid.nearby.NearbyWrapper
import de.smartsquare.kickdroid.statistics.StatisticsActivity
import de.smartsquare.kickdroid.user.User
import de.smartsquare.kickdroid.user.UserDialog
import de.smartsquare.kickdroid.user.UserManager
import kotterknife.bindView
import org.koin.android.ext.android.inject

/**
 * @author Ruben Gees
 */
class MainActivity : BaseActivity() {

    private val toolbar by bindView<Toolbar>(R.id.toolbar)

    private val headline by bindView<TextView>(R.id.headline)
    private val subhead by bindView<TextView>(R.id.subhead)

    private val startMatchButton by bindView<View>(R.id.startMatchButton)
    private val startMatchIcon by bindView<IconicsImageView>(R.id.startMatchIcon)
    private val startMatchText by bindView<TextView>(R.id.startMatchText)
    private val startMatchStatus by bindView<TextView>(R.id.startMatchStatus)
    private val bestPlayersButton by bindView<View>(R.id.bestPlayersButton)

    private val userManager by inject<UserManager>()
    private val nearbyClient by activityInject<NearbyWrapper>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupUI(userManager.user)

        userManager.userChanges()
            .autoDisposable(this.scope())
            .subscribe { setupUI(it.toNullable()) }
    }

    override fun onStart() {
        super.onStart()

        nearbyClient.foundMessages()
            .autoDisposable(this.scope())
            .subscribe {
                // TODO: Show Player matchup screen
            }
    }

    private fun setupUI(user: User?) {
        if (user == null) {
            headline.text = getString(R.string.main_welcome)
            subhead.text = getString(R.string.main_set_name)

            startMatchIcon.icon = startMatchIcon.icon.colorRes(R.color.icon)
            startMatchText.text = getString(R.string.main_matches_not_available)
            startMatchStatus.visibility = View.VISIBLE

            startMatchButton.setOnClickListener(null)
            startMatchButton.isClickable = false
        } else {
            headline.text = getString(R.string.main_welcome_back, user.name)
            subhead.text = getString(R.string.main_status_no_games)

            startMatchIcon.icon = startMatchIcon.icon.colorRes(R.color.colorPrimary)
            startMatchText.text = getString(R.string.main_start_match)
            startMatchStatus.visibility = View.GONE

            startMatchButton.setOnClickListener {
                // TODO
            }
        }

        headline.setOnClickListener { UserDialog.show(this) }
        subhead.setOnClickListener { UserDialog.show(this) }

        bestPlayersButton.setOnClickListener {
            StatisticsActivity.navigateTo(this)
        }
    }
}
