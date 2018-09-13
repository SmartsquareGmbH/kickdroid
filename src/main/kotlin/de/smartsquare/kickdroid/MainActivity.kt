package de.smartsquare.kickdroid

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.gojuno.koptional.rxjava2.filterSome
import com.gojuno.koptional.toOptional
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickdroid.base.BaseActivity
import de.smartsquare.kickdroid.game.GameViewModel
import de.smartsquare.kickdroid.game.IdleFragment
import de.smartsquare.kickdroid.game.MatchmakingFragment
import de.smartsquare.kickdroid.game.PlayingFragment
import de.smartsquare.kickdroid.game.SearchingFragment
import de.smartsquare.kickdroid.statistics.StatisticsActivity
import de.smartsquare.kickdroid.user.User
import de.smartsquare.kickdroid.user.UserDialog
import de.smartsquare.kickdroid.user.UserManager
import de.smartsquare.kickprotocol.KickprotocolDiscoveryException
import io.reactivex.Observable
import kotterknife.bindView
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * @author Ruben Gees
 */
class MainActivity : BaseActivity() {

    private val root by bindView<ViewGroup>(android.R.id.content)
    private val toolbar by bindView<Toolbar>(R.id.toolbar)

    private val headline by bindView<TextView>(R.id.headline)
    private val subhead by bindView<TextView>(R.id.subhead)
    private val bestPlayersButton by bindView<View>(R.id.bestPlayersButton)

    private val userManager by inject<UserManager>()
    private val viewModel by viewModel<GameViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupUI(userManager.user)

        initKickprotocol()
        initListeners()
        initViewModel()
    }

    private fun setupUI(user: User?) {
        if (user == null) {
            headline.text = getString(R.string.main_welcome)
            subhead.text = getString(R.string.main_set_name)
        } else {
            headline.text = getString(R.string.main_welcome_back, user.name)
            subhead.text = getString(R.string.main_status_no_games)
        }
    }

    private fun initKickprotocol() {
        RxPermissions(this)
            .requestEachCombined(Manifest.permission.ACCESS_COARSE_LOCATION)
            .doOnNext {
                if (it.shouldShowRequestPermissionRationale) {
                    // TODO
                } else if (it.granted.not()) {
                    // TODO
                }
            }
            .filter { it.granted }
            .flatMap {
                Observable.merge(
                    Observable.just(userManager.user.toOptional()),
                    userManager.userChanges()
                )
            }
            .filterSome()
            .take(1)
            .singleOrError()
            .autoDisposable(this.scope())
            .subscribe { _ -> viewModel.discover() }
    }

    private fun initListeners() {
        Observable.merge(headline.clicks(), subhead.clicks())
            .filter { viewModel.state.value == GameViewModel.GameState.SEARCHING }
            .autoDisposable(this.scope())
            .subscribe { UserDialog.show(this) }

        bestPlayersButton.clicks()
            .autoDisposable(this.scope())
            .subscribe { StatisticsActivity.navigateTo(this) }

        userManager.userChanges()
            .autoDisposable(this.scope())
            .subscribe { setupUI(it.toNullable()) }
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {
            if (it == null) throw IllegalStateException("state cannot be null")

            val newFragment = when (it) {
                GameViewModel.GameState.SEARCHING -> SearchingFragment.newInstance()
                GameViewModel.GameState.IDLE -> IdleFragment.newInstance()
                GameViewModel.GameState.MATCHMAKING -> MatchmakingFragment.newInstance()
                GameViewModel.GameState.PLAYING -> PlayingFragment.newInstance()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.gameContainer, newFragment)
                .commitNow()
        })

        viewModel.error.observe(this, Observer {
            if (it is KickprotocolDiscoveryException) {
                Snackbar.make(root, "Fehler bei der Suche nach Geräten", Snackbar.LENGTH_LONG).show()
            }
        })
    }
}
