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

import androidx.appcompat.app.AppCompatActivity

/**
 * A base implementation of the [View] interface that bind and unbind it to the store. Note:
 * All Views should extend this to get RxMvi functionality.
 */
public abstract class RxMviView<State, ViewModel: RxMviViewModel<State>>: AppCompatActivity(), View<State> {
    public abstract val viewModel: ViewModel

    /**
     * Renders the state of the store to the UI
     */
    abstract override fun render(state: State)

    override fun onPause() {
        super.onPause()
        viewModel.unbind()
    }

    override fun onResume() {
        super.onResume()
        viewModel.bind(this)
    }
}