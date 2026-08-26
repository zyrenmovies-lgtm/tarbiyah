package com.tarbiyah.ailearn.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.tarbiyah.ailearn.databinding.ItemFeedPostBinding

data class FeedPost(
    val id: String = "",
    val username: String = "",
    val content: String = "",
    val timeAgo: String = "",
    val likes: Int = 0,
    val comments: Int = 0,
    val isFollowing: Boolean = false
)

class FeedAdapter(
    private var posts: MutableList<FeedPost> = mutableListOf()
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    inner class FeedViewHolder(private val binding: ItemFeedPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: FeedPost) {
            binding.tvPostUsername.text = "@${post.username}"
            binding.tvPostContent.text = post.content
            binding.tvPostTime.text = post.timeAgo
            binding.tvLikeCount.text = post.likes.toString()
            binding.tvCommentCount.text = post.comments.toString()

            binding.btnFollow.text = if (post.isFollowing) "Mengikuti" else "Ikuti"
            binding.btnFollow.alpha = if (post.isFollowing) 0.6f else 1f

            binding.btnFollow.setOnClickListener {
                Toast.makeText(binding.root.context, "Mengikuti @${post.username}", Toast.LENGTH_SHORT).show()
            }

            binding.btnLike.setOnClickListener {
                Toast.makeText(binding.root.context, "MasyaAllah!", Toast.LENGTH_SHORT).show()
            }

            binding.btnComment.setOnClickListener {
                Toast.makeText(binding.root.context, "Fitur komentar segera hadir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemFeedPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size

    fun submitList(newPosts: List<FeedPost>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    fun addPost(post: FeedPost) {
        posts.add(0, post)
        notifyItemInserted(0)
    }
}
