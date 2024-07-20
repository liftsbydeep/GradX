package com.example.gradx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [connect.newInstance] factory method to
 * create an instance of this fragment.
 */
class connect : Fragment() {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()
    private val db: FirebaseFirestore = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connect, container, false)

        searchView = view.findViewById(R.id.search_bar)
        recyclerView = view.findViewById(R.id.recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(context)
        userAdapter = UserAdapter(userList){
                user ->
            openUserProfile(user)
        }
        recyclerView.adapter = userAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText)
                return true
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchUsers()
    }

    private fun fetchUsers() {
        db.collection("USERS").get()
            .addOnSuccessListener { documents ->
                userList.clear()
                for (document in documents) {
                    val user = document.toObject(User::class.java).copy(uuid = document.id)
                    Log.d("Connect", "Fetched user: ${user.name}, ${user.email}")
                    userList.add(user)
                }
                userAdapter.updateUsers(userList)
                Log.d("Connect", "Fetched ${userList.size} users")
            }
            .addOnFailureListener { exception ->
                Log.e("Connect", "Error fetching users", exception)
                Toast.makeText(context, "Error fetching users: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterUsers(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            userList
        } else {
            userList.filter {
                it.name.contains(query, true) || it.email.contains(query, true)
            }
        }
        userAdapter.updateUsers(filteredList)
    }

    private fun openUserProfile(user: User) {
        Log.d("Connect", "Opening profile for user with UUID: ${user.uuid}")
        val intent = Intent(context, UserProfile::class.java).apply {
            putExtra("USER_ID", user.uuid)
        }
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance() = connect()
    }
}