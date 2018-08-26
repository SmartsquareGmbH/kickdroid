package de.smartsquare.kickdroid.base

import android.content.Context
import android.view.inputmethod.InputMethodManager

val Context.inputMethodManager get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
