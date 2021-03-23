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
package com.maximcode.demoapp.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.jakewharton.rxbinding4.view.clicks
import com.maximcode.rxmvi.view.RxMviActivity
import com.maximcode.demoapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_counter.*

@AndroidEntryPoint
class CounterActivity : RxMviActivity<CounterState, CounterViewModel>() {
    override val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)
        bindActions()
    }

    override fun render(state: CounterState) {
        renderCounter(state)
        renderHint(state)

    }

    private fun bindActions() {
        viewModel.incrementCounter(incBtnView.clicks())
        viewModel.decrementCounter(decBtnView.clicks())
        viewModel.showHint(showHintBtnView.clicks())
    }

    private fun renderHint(state: CounterState) {
        when {
            state.isHintDisplayed -> {
                showHintBtnView.text = getString(R.string.hide_hint_btn)
                hintView.visibility = View.VISIBLE
            }

            !state.isHintDisplayed -> {
                showHintBtnView.text = getString(R.string.show_hint_btn)
                hintView.visibility = View.GONE
            }
        }
    }

    private fun renderCounter(state: CounterState) {
        when {
            state.isCalculating -> {
                progressView.visibility = View.VISIBLE
                counterGroup.visibility = View.GONE
            }
            else -> {
                progressView.visibility = View.GONE
                counterGroup.visibility = View.VISIBLE
                counterView.text = state.result.toString()
            }
        }
    }
}
