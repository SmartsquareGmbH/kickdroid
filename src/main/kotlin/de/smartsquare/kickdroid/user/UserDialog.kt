package de.smartsquare.kickdroid.user

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.editorActionEvents
import com.jakewharton.rxbinding2.widget.textChanges
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickdroid.R
import de.smartsquare.kickdroid.base.BaseDialog
import de.smartsquare.kickdroid.base.inputMethodManager
import kotterknife.bindView
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import java.util.UUID

/**
 * @author Ruben Gees
 */
class UserDialog : BaseDialog() {

    companion object {
        private const val TAG = "user_dialog"

        fun show(activity: FragmentActivity) {
            UserDialog().show(activity.supportFragmentManager, TAG)
        }
    }

    private val userManager by inject<UserManager>()
    private val viewModel by viewModel<UserDialogViewModel>()

    private val contentContainer by bindView<ViewGroup>(R.id.contentContainer)
    private val nameInputContainer by bindView<TextInputLayout>(R.id.nameInputContainer)
    private val nameInput by bindView<EditText>(R.id.nameInput)
    private val errorText by bindView<TextView>(R.id.errorText)
    private val progress by bindView<ProgressBar>(R.id.progress)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(context, R.layout.dialog_user, null)

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.user_name_title)
            .setView(view)
            .setPositiveButton(R.string.user_set_action, null)
            .setNegativeButton(R.string.user_cancel_action, null)
            .create()
            .apply {
                setOnShowListener {
                    if (savedInstanceState == null) {
                        nameInput.setText(userManager.user?.name ?: "")
                        nameInput.setSelection(nameInput.text.length)
                    }

                    nameInput.requestFocus()

                    requireContext().inputMethodManager.showSoftInput(nameInput, InputMethodManager.SHOW_IMPLICIT)
                }
            }
    }

    override fun onStart() {
        super.onStart()

        viewModel.authorizationSuccess.observe(this, Observer {
            if (it != null) dismiss()
        })

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
                contentContainer.visibility = View.INVISIBLE
                progress.visibility = View.VISIBLE
            } else {
                contentContainer.visibility = View.VISIBLE
                progress.visibility = View.INVISIBLE
            }
        })

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).clicks()
            .autoDisposable(this.scope())
            .subscribe { authorize() }

        nameInput.textChanges()
            .autoDisposable(this.scope())
            .subscribe { setError(null) }

        nameInput.editorActionEvents()
            .filter { it.actionId() == EditorInfo.IME_ACTION_GO }
            .autoDisposable(this.scope())
            .subscribe { authorize() }
    }

    private fun authorize() {
        val name = nameInput.text.toString().trim()

        if (name.isBlank()) {
            setError(getString(R.string.user_error_empty))
        } else {
            val id = UUID.randomUUID().toString()
            val user = userManager.user?.copy(name = name) ?: User(id, name)

            viewModel.authorize(user)
        }
    }

    private fun setError(message: String?) {
        nameInputContainer.isErrorEnabled = message != null
        nameInputContainer.error = message
    }
}
