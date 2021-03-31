package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.util.regex.Matcher
import java.util.regex.Pattern

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
    private fun isUserExist(login: String) = map[login] != null


    fun registerUserByPhone(fullName: String, rawPhone: String): User {
        if (!isValidPhone(rawPhone))
            throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
        val user = User.makeUser(fullName, phone = rawPhone)
        val login = user.login

        if (map[login] != null)
            throw IllegalArgumentException("A user with this phone already exists")
        map[login] = user
        return user
    }


    fun isValidPhone(phone: String):Boolean{
        var number_phone = ""
        for (ch in phone.indices){
            if (phone[ch].isDigit())
                number_phone += phone[ch]
        }
        if(number_phone.length != 11)
            return false
        return true
    }

    // Авторизация пользователя
    fun loginUser(login: String, password: String):String? {
        return map[login.trim()]?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    // Авторизация пользователя
    fun loginUser1(login: String, password: String):String? {
        println("login = $login password = $password")
        map[login.trim()]?.let { user ->
            if (isUserExist(login))
                println("user!!!!!!!! $map[$login]")
            if(user.checkPassword(password)) {
                println("${user.userInfo}")
                return user.userInfo
            }else{
                return null
            }
        }
        return null
    }

    // Запрос кода авторизации
    fun  requestAccessCode(login: String) : Unit {
    }



    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
        map.clear()
    }
}