package de.smartsquare.kickchain.android.client

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.mikepenz.iconics.view.IconicsImageView
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickchain.android.client.nearby.NearbyWrapper
import de.smartsquare.kickchain.android.client.user.User
import de.smartsquare.kickchain.android.client.user.UserDialog
import de.smartsquare.kickchain.android.client.user.UserManager
import kotterknife.bindView
import org.koin.android.ext.android.inject

/**
 * @author Ruben Gees
 */
class MainActivity : BaseActivity() {

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
        setupUI(userManager.user)

        userManager.userChanges()
            .autoDisposable(AndroidLifecycleScopeProvider.from(this))
            .subscribe { setupUI(it.toNullable()) }

        nearbyClient.foundMessages()
            .autoDisposable(AndroidLifecycleScopeProvider.from(this))
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
            // TODO
        }
    }
}
