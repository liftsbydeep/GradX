package com.example.gradx

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.PropertyName
import de.hdodenhof.circleimageview.CircleImageView

data class User(
    @field:JvmField @PropertyName("uuid") val uuid: String = "",
    @field:JvmField @PropertyName("Name") val name: String = "",
    @field:JvmField @PropertyName("Email") val email: String = "",
    @field:JvmField @PropertyName("profileImageUrl") val profileImageUrl: String = "",

)
class UserAdapter(private var users: List<User>,
                  private val onUserClick: (User) -> Unit)
                  : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View,
                         private val onUserClick: (User) -> Unit) :
                         RecyclerView.ViewHolder(view) {

        private val nameTextView: TextView = view.findViewById(R.id.userName)
        private val emailTextView: TextView = view.findViewById(R.id.userEmail)
        private val profileImageView: CircleImageView = view.findViewById(R.id.profilepic1)

        fun bind(user: User) {
            nameTextView.text = user.name
            emailTextView.text = user.email

            Glide.with(itemView.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.baseline_people_alt_24)
                .into(profileImageView)

            itemView.setOnClickListener{
                onUserClick(user)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view,onUserClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        Log.d("UserAdapter", "Updating with ${newUsers.size} users")
        notifyDataSetChanged()
    }
}