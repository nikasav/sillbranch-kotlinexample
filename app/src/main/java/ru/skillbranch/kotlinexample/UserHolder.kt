package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {

    private var map = mutableMapOf<String, User>()

    fun createMap(){
        map = mutableMapOf( "Katy" to User("Katy", "Petrova", "nnnnn@mm.com", "1111"),
            "Lili" to User("Lili", "Sodorova", "wwww@ww.com", "2222"),
            "Jone" to User("Jone", "Smit", "ddddd@jjj.com", "0000")
        )
    }

    fun printMap(){
        for(key in map.keys){
            println("---")
            println("$key : [${map[key]}] . ${map[key].toString()}")
        }
    }


    fun registerUser(fullName:String, email:String, password:String): User {
        var user = User.makeUser(fullName, email, password)

        for (key in map.keys) {
            if ((map[key]?.login ?: user.login) as Boolean) {
                return throw  IllegalArgumentException("A user with this email already exists")
            }
        }
        return user
    }



    fun loginUser(login:String, password: String):String? =
        map[login.trim()]?.let {
            if(it.checkPassword(password)) it.userInfo
            else null
        }


    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
        map.clear()
    }
}