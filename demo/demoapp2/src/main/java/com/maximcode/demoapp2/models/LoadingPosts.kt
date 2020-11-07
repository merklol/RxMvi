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
package com.maximcode.demoapp2.models

import com.maximcode.demoapp2.utils.Mapper
import com.maximcode.demoapp2.dto.Post
import com.maximcode.demoapp2.dto.PostEntity
import com.maximcode.demoapp2.network.TypicodeAPI
import com.maximcode.demoapp2.posts.Actions
import com.maximcode.demoapp2.posts.Effects
import com.maximcode.demoapp2.posts.PostsState
import com.maximcode.rxmvi.core.Middleware
import com.maximcode.rxmvi.core.actions.Action
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class LoadingPosts(
    private val typicodeAPI: TypicodeAPI,
    private val mapper: Mapper<PostEntity, Post>): Middleware<PostsState> {

    override fun bind(state: Observable<PostsState>, actions: Observable<Action>): Observable<Action> {
        return actions.ofType(Actions.Load::class.java)
            .withLatestFrom(state) { action, currentState -> action to currentState }
            .flatMap {
                typicodeAPI.posts()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map<Effects> { result ->
                        Effects.Loaded(result.map { mapper.mapFromEntity(it) })
                    }
                    .onErrorReturn { Effects.Failed(it) }
                    .startWith(Observable.just(Effects.Loading))
            }
    }
}