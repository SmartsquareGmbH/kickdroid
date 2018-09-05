@file:Suppress("NOTHING_TO_INLINE")

package de.smartsquare.kickdroid.base

import android.content.Context
import android.content.res.Resources
import android.view.inputmethod.InputMethodManager
import androidx.annotation.PluralsRes

inline val Context.inputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

inline fun Resources.getSimpleQuantityString(@PluralsRes id: Int, amount: Int): String {
    return getQuantityString(id, amount, amount)
}
