package com.example.gradx

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.gradx.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Profile : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var followingListener: ListenerRegistration? = null
    private var followersListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            fetchUserDetails(currentUser)
            listenForFollowingChanges(currentUser.email ?: "")
        } else {
            context?.let {
                Toast.makeText(it, "User ID not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserDetails(user: FirebaseUser) {
        val userId = user.email
        Log.d("Profile", "Fetching details for user with ID: $userId")
        binding.progressBar8.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot = userId?.let { db.collection("USERS").document(it).get().await() }
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        val userData = documentSnapshot.data ?: emptyMap()
                        val updatedUserData = userData.toMutableMap().apply {
                            putIfAbsent("following", 0L)
                            putIfAbsent("followers", 0L)
                        }

                        // Update the document with the new fields if they were added
                        if (updatedUserData.size > userData.size) {
                            createOrUpdateUserDocument(userId, updatedUserData)
                        }

                        withContext(Dispatchers.Main) {
                            updateUI(
                                updatedUserData["qualification"] as? String ?: "N/A",
                                updatedUserData["institutionName"] as? String ?: "N/A",
                                updatedUserData["workedfor1st"] as? String ?: "N/A",
                                updatedUserData["workedfor2nd"] as? String ?: "N/A",
                                updatedUserData["hometown"] as? String ?: "N/A",
                                updatedUserData["Name"] as? String ?: "N/A",
                                updatedUserData["profileImageUrl"] as? String,
                                updatedUserData["workingat"] as? String ?: "N/A",
                                updatedUserData["experience"] as? String ?: "N/A",
                                updatedUserData["workedfor"] as? String ?: "N/A",
                                updatedUserData["skillset"] as? String ?: "N/A",
                                updatedUserData["workcity"] as? String ?: "N/A",
                                updatedUserData["designation"] as? String
                            )

                            // Update follower and following counts
                            binding.textView222.text = (updatedUserData["followers"] as? Long ?: 0).toString()
                            binding.textView224.text = (updatedUserData["following"] as? Long ?: 0).toString()
                        }
                    } else {
                        // Create a new user document if it doesn't exist
                        val newUserData = hashMapOf(
                            "Name" to "New User",
                            "followingCount" to 0L,
                            "followerCount" to 0L
                            // Add other default fields as needed
                        )
                        createOrUpdateUserDocument(userId, newUserData)

                        withContext(Dispatchers.Main) {
                            context?.let {
                                Toast.makeText(it, "Created new user profile", Toast.LENGTH_SHORT).show()
                            }
                            updateUI("N/A", "N/A", "N/A", "N/A", "N/A", "New User", null, "N/A", "N/A", "N/A", "N/A", "N/A", null)
                            binding.textView222.text = "0"
                            binding.textView224.text = "0"
                        }
                    }
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    context?.let {
                        Toast.makeText(it, "Error: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBar8.visibility = View.GONE
                }
            }
        }
    }

    private fun createOrUpdateUserDocument(userId: String, userData: Map<String, Any>) {
        db.collection("USERS").document(userId)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Profile", "User document successfully created/updated")
            }
            .addOnFailureListener { e ->
                Log.w("Profile", "Error creating/updating user document", e)
            }
    }

    private fun updateUI(
        qualification: String,
        institutionName: String,
        workedfor1st: String,
        workedfor2nd: String,
        hometown: String,
        userName: String,
        userProfileImageUrl: String?,
        workingat: String?,
        experience: String?,
        workedfor: String?,
        skillset: String?,
        workcity: String?,
        designation: String?
    ) {
        binding.textView4.text = qualification
        binding.textView5.text = institutionName
        binding.textView6.text = workedfor1st
        binding.textView7.text = workedfor2nd
        binding.textView8.text = hometown
        binding.textView0.text = userName
        binding.textView9.text = workingat ?: "N/A"
        binding.textView10.text = experience ?: "N/A"
        binding.textView111.text = workedfor ?: "N/A"
        binding.textView11.text = skillset ?: "N/A"
        binding.textView2.text = workingat ?: "N/A"
        binding.textView3.text = workcity ?: "N/A"
        binding.textView1.text = designation ?: "N/A"

        userProfileImageUrl?.let {
            Glide.with(this@Profile)
                .load(it)
                .placeholder(R.drawable.baseline_people_alt_24)
                .into(binding.profilepic2)
        }
    }

    private fun listenForFollowingChanges(userId: String) {
        FirebaseFirestore.getInstance().collection("USERS").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    Log.w("Profile", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val followingCountValue = snapshot.getLong("following")?:0
                    val followerCountValue = snapshot.getLong("followers")?:0

                    updateCountSafely(binding.textView224, followingCountValue)
                    updateCountSafely(binding.textView222, followerCountValue)

                    Log.d("Profile", "Updated counts - Following: $followingCountValue, Followers: $followerCountValue")
                }
            }
    }
    private fun updateCountSafely(textView: TextView, count: Long) {
        val safeCount = count.coerceAtLeast(0)
        textView.text = safeCount.toString()
    }

    companion object {
        @JvmStatic
        fun newInstance() = Profile()
    }
}