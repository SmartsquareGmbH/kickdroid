/*
 * Copyright (c) 2017. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.recyclerview.widget

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.uber.autodispose.ScopeProvider
import io.reactivex.CompletableSource
import io.reactivex.subjects.CompletableSubject

/**
 * Example implementation of a [ViewHolder] implementation that implements
 * [ScopeProvider]. This could be useful for cases where you have subscriptions that should be
 * disposed upon unbinding or otherwise aren't overwritten in future binds.
 */
abstract class AutoDisposeViewHolder(itemView: View) : BindAwareViewHolder(itemView), ScopeProvider {

    private var unbindNotifier: CompletableSubject? = null

    private val notifier: CompletableSubject
        get() = synchronized(this) {
            return unbindNotifier ?: CompletableSubject.create().also { unbindNotifier = it }
        }

    override fun onUnbind() {
        emitUnbindIfPresent()
        unbindNotifier = null
    }

    private fun emitUnbindIfPresent() {
        unbindNotifier?.let {
            if (!it.hasComplete()) {
                it.onComplete()
            }
        }
    }

    override fun requestScope(): CompletableSource {
        return notifier
    }
}
