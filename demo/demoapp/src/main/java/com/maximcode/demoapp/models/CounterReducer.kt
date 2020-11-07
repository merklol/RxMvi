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

import com.maximcode.rxmvi.core.Reducer
import com.maximcode.rxmvi.core.actions.Action
import com.maximcode.demoapp.main.MainEffect.IncrementSuccess
import com.maximcode.demoapp.main.MainAction.Decrement
import com.maximcode.demoapp.main.MainEffect.Calculating
import com.maximcode.demoapp.main.MainAction.ShowHint
import com.maximcode.demoapp.main.MainAction.HideHint
import com.maximcode.demoapp.main.CounterState

class CounterReducer: Reducer<CounterState> {
    override fun reduce(state: CounterState, action: Action): CounterState {
        return when (action) {
            is IncrementSuccess -> state.copy(
                isCalculating = false,
                isHintDisplayed = false,
                result = action.payload
            )

            is Calculating -> state.copy(
                isCalculating = true,
                isHintDisplayed = false,
                result = state.result
            )

            is ShowHint -> state.copy(
                isCalculating = false,
                isHintDisplayed = true,
                result = state.result
            )

            is HideHint -> state.copy(
                isCalculating = false,
                isHintDisplayed = false,
                result = state.result
            )

            is Decrement -> state.copy(
                isCalculating = false,
                isHintDisplayed = false,
                result = state.result - 1
            )

            else -> state
        }
    }
}