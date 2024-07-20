package com.example.gradx

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.gradx.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Profile : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


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
        } else {
            context?.let {
                Toast.makeText(it, "User ID not found", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun fetchUserDetails(user: FirebaseUser) {
        val userId = auth.currentUser?.email
        Log.d("Profile", "Fetching details for user with ID: $userId")
        binding.progressBar8.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot = userId?.let { db.collection("USERS").document(it).get().await() }
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        val qualification = documentSnapshot.getString("qualification") ?: "N/A"
                        val institutionName = documentSnapshot.getString("institutionName") ?: "N/A"
                        val workedfor1st = documentSnapshot.getString("workedfor1st") ?: "N/A"
                        val workedfor2nd = documentSnapshot.getString("workedfor2nd") ?: "N/A"
                        val hometown = documentSnapshot.getString("hometown") ?: "N/A"
                        val userName = documentSnapshot.getString("Name") ?: "N/A"
                        val userProfileImageUrl = documentSnapshot.getString("profileImageUrl")
                        val workingat = documentSnapshot.getString("workingat")
                        val experience = documentSnapshot.getString("experience")
                        val workedfor = documentSnapshot.getString("workedfor")
                        val skillset = documentSnapshot.getString("skillset")
                        val workcity = documentSnapshot.getString("workcity")
                        val designation = documentSnapshot.getString("designation")

                        withContext(Dispatchers.Main) {
                            updateUI(qualification, institutionName, workedfor1st, workedfor2nd, hometown, userName, userProfileImageUrl, workingat, experience, workedfor, skillset, workcity, designation)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            context?.let {
                                Toast.makeText(it, "No details found for this user", Toast.LENGTH_SHORT).show()
                            }
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

    companion object {
        @JvmStatic
        fun newInstance() = Profile()
    }
}