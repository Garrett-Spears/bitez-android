package com.garrett.bitez.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.garrett.bitez.R

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.feed_recycler_view)
        val feedAdapter: FeedAdapter = FeedAdapter(emptyList())

        recyclerView.adapter = feedAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        homeViewModel.posts.observe(viewLifecycleOwner, Observer {
            posts -> feedAdapter.updatePosts(posts)
        })
    }
}