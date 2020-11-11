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
package com.maximcode.rxmvi

import com.maximcode.rxmvi.core.Middleware
import com.maximcode.rxmvi.core.Middleware.Companion.middlewares
import com.maximcode.rxmvi.core.Reducer
import com.maximcode.rxmvi.core.actions.Action
import com.maximcode.rxmvi.core.store.createStore
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

internal class TestStoreImpl {
    @MockK
    lateinit var reducer: Reducer<TestState>

    private val testMiddleware = Middleware<TestState> { _, actions ->
        return@Middleware actions.ofType(TestActions.Action2::class.java)
            .flatMap { Observable.just(actions).map { TestEffects.Effect } }
    }

    @Before
    internal fun setup() {
        MockKAnnotations.init(this)
        every { reducer.reduce(any(), any()) } answers { TestState() }
    }

    @Test
    internal fun `when an action dispatched, the state should change`() {
        val testObserver = TestObserver<TestState>()
        val store = createStore(reducer, TestState())
        store.state.subscribe(testObserver)

        store.dispatch { TestActions.Action }
        testObserver.assertValueCount(2)
    }

    @Test
    internal fun `check effect triggers, when we use a middleware`() {
        val testObserver = TestObserver<Action>()
        val store = createStore(reducer, TestState(), middlewares(testMiddleware))
        store.actions.subscribe(testObserver)

        store.dispatch { TestActions.Action2 }
        assertThat(testObserver.values()[0], instanceOf(TestEffects.Effect::class.java))
    }
}