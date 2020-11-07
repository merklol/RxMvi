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
package com.maximcode.demoapp.models

import com.maximcode.rxmvi.core.Middleware
import com.maximcode.rxmvi.core.actions.Action
import com.maximcode.demoapp.main.MainEffect
import com.maximcode.demoapp.main.MainAction
import com.maximcode.demoapp.main.CounterState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class IncrementMiddleware: Middleware<CounterState> {
    override fun bind(state: Observable<CounterState>, actions: Observable<Action>): Observable<Action> {
        return actions.ofType(MainAction.Increment::class.java)
            .withLatestFrom(state) { action, currentState -> action to currentState }
            .flatMap { (action, state) ->
                Observable.just(action.value)
                    .delay(5, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map<MainEffect> { value -> MainEffect.IncrementSuccess(state.result + value) }
                    .startWith(Observable.just(MainEffect.Calculating))
            }
    }
}