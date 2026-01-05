package com.example.turoapp.models

object UserSession {
    var firstName : String? = null
    var lastName : String? = null
    var email : String? = null
    var uid : String? = null
    var isOwner : Boolean? = null
    fun resetSession(){
        firstName = null
        lastName = null
        email = null
        uid = null
        isOwner = null
    }
}