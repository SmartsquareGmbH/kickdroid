package de.smartsquare.kickchain.android.client

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.mikepenz.iconics.view.IconicsImageView
import de.smartsquare.kickchain.android.client.findmatch.FindMatchActivity
import de.smartsquare.kickchain.android.client.user.User
import de.smartsquare.kickchain.android.client.user.UserDialog
import de.smartsquare.kickchain.android.client.user.UserManager
import kotterknife.bindView

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

    private val user: User?
        get() = UserManager.getUser(this)

    private var userChangeListener: ((User?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setupUI()

        userChangeListener = { _: User? -> setupUI() }
            .also { UserManager.registerUserChangeListener(this, it) }
    }

    override fun onDestroy() {
        userChangeListener?.also {
            UserManager.unregisterUserChangeListener(this, it)

            userChangeListener = null
        }

        super.onDestroy()
    }

    private fun setupUI() {
        val safeUser = user

        if (safeUser == null) {
            headline.text = getString(R.string.main_welcome)
            subhead.text = getString(R.string.main_set_name)

            startMatchIcon.icon = startMatchIcon.icon.colorRes(R.color.icon)
            startMatchText.text = getString(R.string.main_matches_not_available)
            startMatchStatus.visibility = View.VISIBLE

            startMatchButton.setOnClickListener(null)
            startMatchButton.isClickable = false
        } else {
            headline.text = getString(R.string.main_welcome_back, safeUser.name)
            subhead.text = getString(R.string.main_status_no_games)

            startMatchIcon.icon = startMatchIcon.icon.colorRes(R.color.colorPrimary)
            startMatchText.text = getString(R.string.main_start_match)
            startMatchStatus.visibility = View.GONE

            startMatchButton.setOnClickListener {
                FindMatchActivity.navigateTo(this)
            }
        }

        headline.setOnClickListener { UserDialog.show(this) }
        subhead.setOnClickListener { UserDialog.show(this) }

        bestPlayersButton.setOnClickListener {
            // TODO
        }
    }
}
