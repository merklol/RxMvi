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
package com.maximcode.demoapp2.di

import com.maximcode.demoapp2.utils.PostMapper
import com.maximcode.demoapp2.network.TypicodeAPI
import com.maximcode.demoapp2.models.LoadingPosts
import com.maximcode.demoapp2.models.PostsReducer
import com.maximcode.demoapp2.posts.PostsState
import com.maximcode.rxmvi.core.Middleware.Companion.middlewares
import com.maximcode.rxmvi.core.store.Store
import com.maximcode.rxmvi.core.store.createStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class PostsModule {

    @Provides
    fun providesRetrofitInstance(): Retrofit {
        return Retrofit.Builder().baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(
            RxJava3CallAdapterFactory.create())
            .build()
    }

    @Provides
    fun providesTypicodeAPI(retrofit: Retrofit): TypicodeAPI {
        return retrofit.create(TypicodeAPI::class.java)
    }

    @Provides
    fun providesStore(typicodeAPI: TypicodeAPI): Store<PostsState> {
        return createStore(
            PostsReducer(), PostsState(), middlewares(LoadingPosts(typicodeAPI, PostMapper()))
        )
    }
}

