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
package com.maximcode.demoapp2.posts

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.maximcode.demoapp2.databinding.ActivityPostsBinding
import com.maximcode.demoapp2.recyclerview.RVAdapter
import com.maximcode.demoapp2.recyclerview.RVMarginDecoration
import com.maximcode.rxmvi.view.RxMviActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostsActivity : RxMviActivity<PostsState, PostsViewModel>() {
    private lateinit var binding: ActivityPostsBinding
    override val viewModel: PostsViewModel by viewModels()

    private val adapter = RVAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setRecyclerView()

        viewModel.loadPosts()
    }

    override fun render(state: PostsState) {
        when {
            state.loading -> binding.progressView.visibility = View.VISIBLE
            state.loaded -> {
                if(state.error != null) {
                    binding.errorView.text = state.error.message
                    binding.errorView.visibility = View.VISIBLE
                }
                binding.progressView.visibility = View.GONE
                adapter.addPosts(state.posts)
            }
        }
    }

    private fun setRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(RVMarginDecoration(this, 16, 16))
    }
}
