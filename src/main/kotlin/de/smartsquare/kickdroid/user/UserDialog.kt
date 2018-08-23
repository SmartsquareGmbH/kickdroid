package de.smartsquare.kickdroid.user

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputLayout
import de.smartsquare.kickdroid.R
import kotterknife.bindView
import org.koin.android.ext.android.inject
import java.util.UUID


/**
 * @author Ruben Gees
 */
class UserDialog : DialogFragment() {

    companion object {
        private const val TAG = "user_dialog"

        fun show(activity: FragmentActivity) {
            UserDialog().show(
                activity.supportFragmentManager,
                TAG
            )
        }
    }

    private val userManager by inject<UserManager>()

    private val nameInputContainer by bindView<TextInputLayout>(R.id.nameInputContainer)
    private val nameInput by bindView<EditText>(R.id.nameInput)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(context, R.layout.dialog_user, null)

        val result = AlertDialog.Builder(requireContext())
            .setTitle(R.string.user_name_title)
            .setView(view)
            .setPositiveButton(R.string.user_set_action, null)
            .setNegativeButton(R.string.user_cancel_action, null)
            .create()

        result.setOnShowListener {
            result.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameInput.text.toString().trim()

                if (name.isEmpty()) {
                    setError(getString(R.string.user_empty_error))
                } else {
                    val id = UUID.randomUUID().toString()

                    userManager.user = userManager.user?.copy(name = name) ?: User(id, name)

                    dismiss()
                }
            }

            nameInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(text: Editable?) = Unit
                override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) = setError(null)
            })

            nameInput.setText(user?.name ?: "")
            nameInput.requestFocus()

            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.showSoftInput(nameInput, InputMethodManager.SHOW_IMPLICIT)
        }

        return result
    }

    private fun setError(message: String?) {
        nameInputContainer.isErrorEnabled = message != null
        nameInputContainer.error = message
    }
}
