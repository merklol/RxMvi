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
import com.maximcode.demoapp.main.MainAction
import com.maximcode.demoapp.main.MainEffect
import com.maximcode.demoapp.models.IncrementMiddleware
import com.maximcode.rxmvi.core.actions.Action
import io.mockk.MockKAnnotations
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class IncrementMiddlewareTest {
    private val state = Observable.just(CounterState())
    private val action: Observable<Action> =  Observable.just(MainAction.Increment(1))
    private val testObserver = TestObserver<Action>()

    @Before
    fun setup() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        val inc = IncrementMiddleware()
        inc.bind(state, action).subscribe(testObserver)
    }

    @Test
    fun `when bind is called, should start with Calculating`() {
        testObserver.assertValue(MainEffect.Calculating)
    }

    @Test
    fun `after delay, should return IncrementSuccess`() {
        testObserver.awaitDone(4, TimeUnit.SECONDS)
        val effect = testObserver.values()[1] as MainEffect.IncrementSuccess
        assertThat(effect.payload, `is`(1))
    }
}