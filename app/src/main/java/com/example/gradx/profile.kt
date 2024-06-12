package com.example.gradx

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gradx.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Profile : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

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
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = requireContext().getSharedPreferences("GradxPrefs", Context.MODE_PRIVATE)

        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)

        if (!isLoggedIn || auth.currentUser == null) {
            startActivity(Intent(requireContext(), Login_Page::class.java))
            requireActivity().finish()
        } else {
            binding.email.text = auth.currentUser?.email
            loadUserData(auth.currentUser?.email ?: "")
        }

        binding.logout.setOnClickListener {
            auth.signOut()
            sharedPreferences.edit().putBoolean("IS_LOGGED_IN", false).apply()
            startActivity(Intent(requireContext(), Login_Page::class.java))
            requireActivity().finish()
        }
    }

    private fun loadUserData(email: String) {
        lifecycleScope.launch {
            try {
                val documents = withContext(Dispatchers.IO) {
                    firestore.collection("USERS").whereEqualTo("Email", email).get().await()
                }
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val name = document.getString("Name")

                    withContext(Dispatchers.Main) {
                        if (name != null) {
                            binding.name.text = name
                        } else {
                            Log.d("Profile", "No name found for email: $email")
                        }
                    }
                } else {
                    Log.d("Profile", "No user found with email: $email")
                }
            } catch (exception: Exception) {
                Log.e("Profile", "Error getting user data: $exception")
            }
        }
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}