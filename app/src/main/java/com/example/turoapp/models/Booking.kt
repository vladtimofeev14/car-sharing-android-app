package com.example.turoapp.models

data class Booking(
    var id: String = "",
    var confirmationCode: String = "",
    var listingId: String = "",
    var ownerId: String = "",
    var renterId: String = "",
    var startDate: String = "",
    var endDate: String = ""
)