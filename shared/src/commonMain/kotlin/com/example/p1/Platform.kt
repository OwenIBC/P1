package com.example.p1

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform