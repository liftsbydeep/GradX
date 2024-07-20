package com.example.gradx

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class Follow_Unfollow {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun followUser(userId: String) {
        val currentUser = auth.currentUser ?: return
        val currentUserId = currentUser.uid
        val followingRef = db.collection("USERS").document(currentUserId).collection("following").document(userId)
        val followersRef = db.collection("USERS").document(userId).collection("followers").document(currentUserId)

        db.runBatch { batch ->
            batch.set(followingRef, mapOf("userId" to userId))
            batch.set(followersRef, mapOf("userId" to currentUserId))

            // Increment following count for current user
            val currentUserRef = db.collection("USERS").document(currentUserId)
            batch.update(currentUserRef, "followingCount", FieldValue.increment(1))

            // Increment followers count for followed user
            val followedUserRef = db.collection("USERS").document(userId)
            batch.update(followedUserRef, "followersCount", FieldValue.increment(1))
        }.await()
    }

    suspend fun unfollowUser(userId: String) {
        val currentUser = auth.currentUser ?: return
        val currentUserId = currentUser.uid
        val followingRef = db.collection("USERS").document(currentUserId).collection("following").document(userId)
        val followersRef = db.collection("USERS").document(userId).collection("followers").document(currentUserId)

        db.runBatch { batch ->
            batch.delete(followingRef)
            batch.delete(followersRef)

            // Decrement following count for current user
            val currentUserRef = db.collection("USERS").document(currentUserId)
            batch.update(currentUserRef, "followingCount", FieldValue.increment(-1))

            // Decrement followers count for unfollowed user
            val followedUserRef = db.collection("USERS").document(userId)
            batch.update(followedUserRef, "followersCount", FieldValue.increment(-1))
        }.await()
    }

    suspend fun isFollowingUser(userId: String): Boolean {
        val currentUser = auth.currentUser ?: return false
        val currentUserId = currentUser.uid
        val followingDoc = db.collection("USERS").document(currentUserId).collection("following").document(userId).get().await()
        return followingDoc.exists()
    }
}