package com.example.gradx

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel:ViewModel() {
    private val _followersCount = MutableLiveData<Long>()
    val followersCount: LiveData<Long> get() = _followersCount

    private val _followingCount = MutableLiveData<Long>()
    val followingCount: LiveData<Long> get() = _followingCount

    fun setFollowersCount(count: Long) {
        _followersCount.value = count
    }

    fun setFollowingCount(count: Long) {
        _followingCount.value = count
    }
}