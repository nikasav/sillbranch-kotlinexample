package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
){
    val userInfo : String

    private val fullName:String
        get() = listOfNotNull(firstName, lastName)
            .joinToString (" ")
            .capitalize()

    private val initials : String
        get() = listOfNotNull(firstName, lastName)
            .map{it.first().toUpperCase()}
            .joinToString(" ")

    private var phone : String? = null
        set(value) { field = value?.replace("[^+\\d]".toRegex(), "")}

    private var _login:String?=null
    var login:String
        set(value){
            _login = value.toLowerCase()
        }
        get() = _login!!

    private lateinit var passwordHash : String
    private val salt : String by lazy {
        ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
    }
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String?=null

    //For email
    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ): this(firstName, lastName, email = email, meta = mapOf("auth" to "password")){
        println("Secondary email constructor")
        passwordHash = encrypt(password)
    }

    // for phone
    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ):this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")){
        println("Secondary phone constructor")
        regenerateAccessCode(rawPhone)
    }

    init{
        println("First init block? primary constructor was called")

        check(!firstName.isBlank()) {throw IllegalArgumentException("FirstName must be not blank")}
        check(email.isNullOrBlank() || rawPhone.isNullOrBlank() ) {throw IllegalArgumentException("Email or phone must not be null or blank")}

        phone = rawPhone
        login = email ?: phone!!

        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    fun checkPassword(pass : String) = encrypt(pass) == passwordHash.also {
        println("Checking passwordHash is $passwordHash")
    }

    fun changePassword(oldPass:String, newPass:String){
        if(checkPassword(oldPass)){
            passwordHash = encrypt(newPass)
            if(!accessCode.isNullOrEmpty()) accessCode = newPass
            println("Password $oldPass has been changed on new password $newPass")
        }else throw IllegalArgumentException("The entered password does not match the current password")
    }

    private fun encrypt(password: String): String = salt.plus(password).md5()

    private fun String.md5():String{
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        val hexStr  = BigInteger(1, digest).toString(16)
        return hexStr.padStart(32, '0')
    }

    fun regenerateAccessCode(rawPhone: String) {
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        println("Phone passwordHash is $passwordHash")
        accessCode = code
        sendAccessCodeToUser(rawPhone, code)
    }

    private fun generateAccessCode():String{
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        return StringBuilder().apply {
            repeat(6){
                (possible.indices).random().also { index->
                    append(possible[index])
                }
            }
        }.toString()
    }

    private fun sendAccessCodeToUser(phone: String, code:String){
        println("... sending access code: $code on $phone")
    }

    companion object Factory{
        fun makeUser(
            fullName    : String,
            email       : String?=null,
            password    : String?=null,
            phone       : String?=null
        ):User {
            val (firstName, lastName) = fullName.fullNameToPairMy()
            return when{
                !phone.isNullOrBlank() -> User(firstName, lastName, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName, lastName, email, password)
                else -> throw IllegalArgumentException("Email or phone must not be null or blank")
            }
        }

        private fun String.fullNameToPair() : Pair<String, String?> =
            this.split(" ")
                .filter { it.isNullOrBlank() }
                .run {
                    when(size) {
                        1-> first() to null
                        2-> first() to last()
                        else -> throw IllegalArgumentException("FullName must contain only first name and last name, current split result : ${this@fullNameToPair}")
                    }
                }

        private fun String.fullNameToPairMy() : Pair<String, String?> {
            val fullName = this
            val res  = fullName.split(" ")
            return when(res.size){
                1 -> res.first() to null
                2 -> res.first() to res.last()
                else -> throw IllegalArgumentException("FullName must contain only first name and last name, current split result : $fullName")
            }
        }
    }
}

