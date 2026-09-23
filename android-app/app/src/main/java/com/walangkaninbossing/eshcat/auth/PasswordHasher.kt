package com.walangkaninbossing.eshcat.auth

object PasswordHasher {

    private const val SALT = "walang-kanin-bossing::eshcat::2026"

    fun hash(password: String): String {
        val data = (password + SALT).toByteArray(Charsets.UTF_8)
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(data)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, hash: String): Boolean =
        hash == hash(password)
}