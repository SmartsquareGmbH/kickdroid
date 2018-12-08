package de.smartsquare.kickdroid.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.editorActionEvents
import com.jakewharton.rxbinding2.widget.textChanges
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickdroid.R
import kotterknife.bindView
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.UUID

class UserFragment : Fragment() {

    companion object {
        fun newInstance(): UserFragment {
            return UserFragment()
        }
    }

    private val viewModel by viewModel<UserViewModel>()
    private val userManager by inject<UserManager>()

    private val user
        get() = userManager.user ?: throw IllegalStateException("user is null")

    private val nameInputContainer by bindView<TextInputLayout>(R.id.nameInputContainer)
    private val nameInput by bindView<EditText>(R.id.nameInput)
    private val errorText by bindView<TextView>(R.id.errorText)
    private val loading by bindView<View>(R.id.loading)
    private val setButton by bindView<Button>(R.id.setButton)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInput.textChanges()
            .autoDisposable(viewLifecycleOwner.scope())
            .subscribe { setError(null) }

        nameInput.editorActionEvents()
            .filter { it.actionId() == EditorInfo.IME_ACTION_GO }
            .map { Unit }
            .mergeWith(setButton.clicks())
            .autoDisposable(viewLifecycleOwner.scope())
            .subscribe { authorize() }

        viewModel.authorizationError.observe(this, Observer {
            if (it != null) {
                errorText.visibility = View.VISIBLE
                errorText.text = getString(UserErrorHandler.handle(it))
            } else {
                errorText.visibility = View.GONE
            }
        })

        viewModel.authorizationLoading.observe(this, Observer {
            if (it != null) {
                setButton.visibility = View.GONE
                loading.visibility = View.VISIBLE
            } else {
                setButton.visibility = View.VISIBLE
                loading.visibility = View.GONE
            }
        })

        viewModel.authorizationSuccess.observe(this, Observer {
            if (it != null) {
                FirebaseAnalytics.getInstance(requireActivity()).logEvent(
                    FirebaseAnalytics.Event.LOGIN, bundleOf(
                        "name" to user.name
                    )
                )
            }
        })
    }

    private fun authorize() {
        if (viewModel.authorizationLoading.value == null) {
            val name = nameInput.text.toString().trim()

            if (name.isBlank()) {
                setError(getString(R.string.user_error_empty))
            } else {
                val id = UUID.randomUUID().toString()

                viewModel.authorize(User(id, name))
            }
        }
    }

    private fun setError(message: String?) {
        nameInputContainer.isErrorEnabled = message != null
        nameInputContainer.error = message
    }
}
