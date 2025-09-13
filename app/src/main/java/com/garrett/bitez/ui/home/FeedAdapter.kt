package com.garrett.bitez.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.garrett.bitez.R
import com.garrett.bitez.data.model.Post
import com.garrett.bitez.data.model.TextPost

class FeedAdapter(private var posts: List<Post>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class PostType(val typeId: Int) {
        TEXT(0);

        companion object {
            fun fromInt(value: Int) = entries.first { it.typeId == value }
        }
    }

    fun updatePosts(newPosts: List<Post>) {
        this.posts = newPosts
        notifyDataSetChanged()
    }

    // Determine type of post for each item in list
    override fun getItemViewType(position: Int): Int {
        val postType: PostType

        when (this.posts[position]) {
            is TextPost -> postType = PostType.TEXT
            else -> throw IllegalArgumentException("Unknown post type for position $position")
        }

        return postType.typeId
    }

    override fun getItemCount(): Int {
        return this.posts.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val postType: PostType = PostType.fromInt(viewType)

        // Figure out what type of view and view holder needs to be created for viewType
        when (postType) {
            PostType.TEXT -> {
                val view: View = inflater.inflate(R.layout.post_text, parent, false)
                return TextPostViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val postData: Post = this.posts[position]

        when (viewHolder) {
            is TextPostViewHolder -> {
                if (postData is TextPost) {
                    viewHolder.bind(postData)
                    return
                }
            }
            else -> {
                throw IllegalArgumentException("Unknown view holder type for post at position $position")
            }
        }

        throw IllegalStateException("Cannot bind post: invalid PostData for ViewHolder at position $position")
    }
}

// Base class for all post view holders of diff types
abstract class BasePostViewHolder<T : Post>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(postData: T)
}

class TextPostViewHolder(itemView: View) : BasePostViewHolder<TextPost>(itemView) {
    private val accountText: TextView = itemView.findViewById<TextView>(R.id.account_name)

    override fun bind(postData: TextPost) {
        accountText.text = postData.userName
    }
}
