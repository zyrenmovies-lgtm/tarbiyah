package com.tarbiyah.ailearn.ui.feed

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tarbiyah.ailearn.databinding.FragmentFeedBinding

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var feedAdapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupPostInput()
        loadSamplePosts()
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter()
        binding.rvFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
        }
    }

    private fun setupPostInput() {
        // Check for salam when typing
        binding.etPostInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim().lowercase()
                if (text.isNotEmpty() && !containsSalam(text)) {
                    binding.tvSalamWarning.visibility = View.VISIBLE
                } else {
                    binding.tvSalamWarning.visibility = View.GONE
                }
            }
        })

        binding.btnPost.setOnClickListener {
            val content = binding.etPostInput.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Tuliskan sesuatu terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!containsSalam(content.lowercase())) {
                Toast.makeText(requireContext(), "Awali pesanmu dengan Salam ya!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Add post to feed
            val newPost = FeedPost(
                id = System.currentTimeMillis().toString(),
                username = "rizki_tarbiyah",
                content = content,
                timeAgo = "Baru saja",
                likes = 0,
                comments = 0
            )
            feedAdapter.addPost(newPost)
            binding.etPostInput.text?.clear()
            binding.tvSalamWarning.visibility = View.GONE
            binding.rvFeed.scrollToPosition(0)
            Toast.makeText(requireContext(), "Postingan berhasil dikirim!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun containsSalam(text: String): Boolean {
        val salamVariations = listOf(
            "assalamualaikum", "assalamualaikum warahmatullahi wabarakatuh",
            "assalamu'alaikum", "assalamu alaikum", "salam", "السلام عليكم"
        )
        return salamVariations.any { text.contains(it) }
    }

    private fun loadSamplePosts() {
        val samplePosts = listOf(
            FeedPost(
                id = "1",
                username = "aisyah_mi",
                content = "Assalamualaikum! Baru selesai belajar Bahasa Indonesia tentang teks eksposisi. Ternyata cara AI Tarbiyah menjelaskan dengan contoh sehari-hari itu mudah banget dipahami. Alhamdulillah!",
                timeAgo = "5 menit lalu",
                likes = 24,
                comments = 3
            ),
            FeedPost(
                id = "2",
                username = "fatih_mts",
                content = "Assalamualaikum teman-teman! Hari ini berhasil hafal surah Al-Mulk ayat 1-10 dengan bantuan Asisten Tahfidz. MasyaAllah, koreksinya akurat banget!",
                timeAgo = "15 menit lalu",
                likes = 47,
                comments = 8
            ),
            FeedPost(
                id = "3",
                username = "rizky_ma",
                content = "Assalamualaikum! Bagi yang sedang ujian kimia, jangan lupa tandai rumus mol dengan metode yang diajarkan AI Tutor ya. InsyaAllah bisa!",
                timeAgo = "32 menit lalu",
                likes = 31,
                comments = 5
            ),
            FeedPost(
                id = "4",
                username = "nur_hidayah",
                content = "Assalamualaikum warahmatullahi wabarakatuh! Alhamdulillah selesai Mode Kejar Target Fiqih minggu ini. 3 bab dalam 2 hari! Jazakallah khairan untuk AI Tarbiyah.",
                timeAgo = "1 jam lalu",
                likes = 68,
                comments = 12
            ),
            FeedPost(
                id = "5",
                username = "ilham_ma3",
                content = "Assalamualaikum! Tip belajar: coba gabungkan hafalan Al-Quran dengan rumus matematika, bikin keduanya lebih mudah diingat. Semoga bermanfaat!",
                timeAgo = "2 jam lalu",
                likes = 19,
                comments = 4
            )
        )
        feedAdapter.submitList(samplePosts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
