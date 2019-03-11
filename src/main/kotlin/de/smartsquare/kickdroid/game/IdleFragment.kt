package de.smartsquare.kickdroid.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import com.gojuno.koptional.rxjava2.filterSome
import com.gojuno.koptional.toOptional
import com.jakewharton.rxbinding2.view.clicks
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickdroid.R
import de.smartsquare.kickdroid.base.BaseFragment
import de.smartsquare.kickdroid.user.UserManager
import kotterknife.bindView
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

/**
 * @author Ruben Gees
 */
class IdleFragment : BaseFragment() {

    companion object {
        fun newInstance() = IdleFragment()
    }

    private val viewModel by sharedViewModel<GameViewModel>()
    private val userManager by inject<UserManager>()

    private val createGame by bindView<Button>(R.id.createGame)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_idle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isLoading.observe(this, Observer {
            if (it == null) throw java.lang.IllegalStateException("isLoading cannot be null")

            createGame.isEnabled = !it
        })

        createGame.clicks()
            .map { userManager.user.toOptional() }
            .filterSome()
            .autoDisposable(this.scope())
            .subscribe { viewModel.createGame() }
    }
}
