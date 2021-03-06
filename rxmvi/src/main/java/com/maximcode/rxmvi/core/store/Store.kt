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
package com.maximcode.rxmvi.core.store

import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import com.maximcode.rxmvi.core.actions.Action
import com.maximcode.rxmvi.view.View
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

public interface Store<State> {

    /**
     * Returns the current action of your application.
     *
     */
    public val actions: PublishRelay<Action>

    /**
     * Returns the current state of your application.
     *
     */
    public val state: BehaviorRelay<State>

    /**
     * Releases the store.
     */
    public fun release()

    /**
     * Binds a view to the store.
     */
    public fun bind(view: View<State>): Disposable

//    /**
//     * Unbinds a view from the store.
//     */
//    public fun unbind()

    /**
     * Dispatches an action to trigger a state change.
     */
    public fun<T> dispatch(uiEvent: Observable<T>, action: (T) -> Action): Disposable

    /**
     * Dispatches an action to trigger a state change.
     */
    public fun dispatch( action: () -> Action): Disposable
}