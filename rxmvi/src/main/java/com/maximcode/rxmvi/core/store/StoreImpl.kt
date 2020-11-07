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

import com.maximcode.rxmvi.view.View
import com.maximcode.rxmvi.core.actions.Action
import com.maximcode.rxmvi.core.Middleware
import com.maximcode.rxmvi.core.Reducer
import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import com.maximcode.rxmvi.utils.plusAssign
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Creates a store(state container) which holds the application's state.
 *
 * @param initialState The initial state of the store.
 * @param reducer A class that emit new states based on the latest one and an incoming event.
 * @param middlewares The list of middlewares. (Optional)
 *
 * @return A store that lets you subscribe to changes.
 */
public typealias createStore<State> = StoreImpl<State>

public class StoreImpl<State>(
    private val reducer: Reducer<State>, initialState: State,
    middlewares: List<Middleware<State>> = emptyList()) : Store<State> {

    private lateinit var bindingDisposable: CompositeDisposable
    private val initDisposable = CompositeDisposable()
    private val state = BehaviorRelay.createDefault(initialState)
    private val actions = PublishRelay.create<Action>()

    public override val currentState: State get() = state.value

    init {
        initDisposable.addAll(
            actions.withLatestFrom(state) { action, state -> reducer.reduce(state, action) }
                .distinctUntilChanged()
                .subscribe(state::accept),
            Observable.merge(middlewares.map { it.bind(state, actions) } )
                .subscribe(actions::accept)
        )
    }

    override fun release(): Unit = initDisposable.dispose()

    override fun bind(view: View<State>) {
        bindingDisposable = CompositeDisposable()
        bindingDisposable += state.subscribe(view::render)
    }

    override fun unbind(): Unit = bindingDisposable.dispose()

    override fun<T> dispatch(uiEvent: Observable<T>, action: (T) -> Action): Disposable =
        uiEvent.map { action(it) }.subscribe(actions::accept)

    override fun dispatch(action: () -> Action): Disposable =
        Observable.just(Unit).map { action() }.subscribe(actions::accept)
}