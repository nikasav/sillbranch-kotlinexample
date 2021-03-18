package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {

    private var map = mutableMapOf<String, User>()

    fun registerUser(fullName:String, email:String, password:String): User {
        val user = User.makeUser(fullName, email, password)
        val login = user.login
        if (isUserExist(login))
            throw IllegalArgumentException("A user with this email already exists")
        map[login] = user
        return user
    }

    private fun isUserExist(login:String) = map[login] != null

    fun loginUser(login:String, password: String):String? =
        map[login.trim()]?.let { user ->
            if(user.checkPassword(password)) user.userInfo
            else null
        }


    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
        map.clear()
    }
}