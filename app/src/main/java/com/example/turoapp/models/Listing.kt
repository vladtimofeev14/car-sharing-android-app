package com.example.turoapp.models

data class Listing (
    val id: String = "",
    val brand: String = "",
    val model: String = "",
    val color: String = "",
    val licensePlate: String = "",
    val cost: Double = 0.0,
    val city: String = "",
    val address: String = "",
    val imageUrl: String = "",
    val createdByUID: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var isBooked: Boolean = false//
)
