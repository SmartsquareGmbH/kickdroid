@file:Suppress("NOTHING_TO_INLINE")

package de.smartsquare.kickdroid.base

import android.content.res.Resources
import androidx.annotation.PluralsRes

inline fun Resources.getSimpleQuantityString(@PluralsRes id: Int, amount: Int): String {
    return getQuantityString(id, amount, amount)
}
