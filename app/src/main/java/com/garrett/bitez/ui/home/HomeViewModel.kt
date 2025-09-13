package com.garrett.bitez.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.garrett.bitez.data.model.Post
import com.garrett.bitez.data.model.TextPost
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _posts: MutableLiveData<List<Post>> = MutableLiveData(emptyList())
    val posts: LiveData<List<Post>> = _posts

    init {
        _posts.value = listOf(
            TextPost("1", "1", "John", "Hello World"),
            TextPost("2", "2", "xdRawr", "Dumplings were good"),
            TextPost("3", "3", "ali_xd", "Yummy boba"),
            TextPost("4", "4", "foodie3000", "Overcooked steak"),
            TextPost("5", "5", "fl_crackhead", "Hate the lighting"),
            TextPost("6", "6", "boobies", "love the service"),
            TextPost("7", "7", "mafia_man", "munch time"),
            TextPost("8", "1", "Jba", "Hmm..."),
            TextPost("9", "2", "fac", "YAHH"),
            TextPost("10", "3", "ca", "Yum!!!"),
            TextPost("11", "4", "vwdwev", "pawg"),
            TextPost("12", "5", "aaa", "lit"),
            TextPost("13", "6", "abdef", "ok cra"),
            TextPost("14", "7", "fuc", "munch time"),
            TextPost("15", "1", "jj_mcarth", "yah no"),
            TextPost("16", "2", "licker", "MCDs"),
            TextPost("17", "3", "ali_69", "Texassss"),
            TextPost("18", "4", "fatFace", "Bullcrap"),
            TextPost("19", "5", "fl_crackhead", "Hat"),
            TextPost("20", "6", "bob", "luv"),
            TextPost("21", "7", "milf", "mun"),
        )
    }
}