/*
 * MIT License
 *
 * Copyright (c) 2020 Maxim Smolyakov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.maximcode.rxmvi.view

import androidx.lifecycle.ViewModel
import com.maximcode.rxmvi.core.store.Store
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Binds and unbinds a view from the store, release it when this ViewModel is no longer used,
 * and dispose actions that come from the UI events.
 *
 * Note: All ViewModels should extend this to get RxMvi functionality.
 *
 */
public abstract class RxMviViewModel<State>(private val store: Store<State>): ViewModel() {
    /**
     * A disposable container that holds onto all actions that are going to be disposed
     * when ViewModel is no longer used and will be destroyed.
     */
    public abstract val disposables: CompositeDisposable

    private var viewBinding: Disposable? = null

    /**
     * Binds a view to the store
     */
    public fun bind(view: View<State>) {
        viewBinding = store.bind(view)
    }

    /**
     * Unbinds a view from the store
     */
    public fun unbind() {
        viewBinding?.dispose()
    }

    override fun onCleared() {
        super.onCleared()
        store.release()
        disposables.dispose()
    }
}