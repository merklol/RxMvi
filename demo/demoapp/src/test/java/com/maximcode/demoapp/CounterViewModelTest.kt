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

package com.maximcode.demoapp

import com.maximcode.demoapp.main.CounterState
import com.maximcode.demoapp.main.CounterViewModel
import com.maximcode.demoapp.main.MainEffect
import com.maximcode.demoapp.models.CounterReducer
import com.maximcode.rxmvi.core.Middleware
import com.maximcode.rxmvi.core.Middleware.Companion.middlewares
import com.maximcode.rxmvi.core.store.createStore
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins.setInitMainThreadSchedulerHandler
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class CounterViewModelTest {
    private lateinit var viewModel: CounterViewModel
    private  val testObserver = TestObserver<CounterState>()
    @MockK
    lateinit var inc: Middleware<CounterState>

    private fun mockMiddleware() {
        val action = MainEffect.IncrementSuccess(1)
        every { inc.bind(any(), any()) } answers { Observable.just(action) }
    }

    @Before
    fun setUp() {
        setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockMiddleware()

        val store = createStore(CounterReducer(), CounterState(), middlewares(inc))
        viewModel = CounterViewModel(store)
        store.state.subscribe(testObserver)

    }

    @Test
    fun `when increment counter triggered, should hide hint and change result to 1`() {
        viewModel.incrementCounter(Observable.just(Unit))
        val state = testObserver.values()[0] as CounterState
        assertThat(state.isHintDisplayed, `is`(false))
        assertThat(state.result, `is`(1))
    }

    @Test
    fun `when decrement counter triggered, should hide hint and change result to 0`() {
        viewModel.decrementCounter(Observable.just(Unit))
        val state = testObserver.values()[1] as CounterState
        assertThat(state.isHintDisplayed, `is`(false))
        assertThat(state.result, `is`(0))

    }

    @Test
    fun `when show hint triggered, should change isHintDisplayed to true`() {
        viewModel.showHint(Observable.just(Unit))
        val state = testObserver.values()[1] as CounterState
        assertThat(state.isHintDisplayed, `is`(true))
    }

    @Test
    fun `when show hint triggered twice, should change isHintDisplayed to false`() {
        viewModel.showHint(Observable.just(Unit))
        viewModel.showHint(Observable.just(Unit))
        val state = testObserver.values()[2] as CounterState
        assertThat(state.isHintDisplayed, `is`(false))
    }
}