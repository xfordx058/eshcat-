package com.walangkaninbossing.eshcat.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtil {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun now(): String = dateTimeFormat.format(Date())

    fun daysAgo(days: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -days.toInt())
        cal.set(java.util.Calendar.HOUR_OF_DAY, 9)
        cal.set(java.util.Calendar.MINUTE, 30)
        return dateTimeFormat.format(cal.time)
    }

    fun todayStamp(): String = now().take(10)

    fun displayDate(raw: String): String {
        val parsed = try { dateTimeFormat.parse(raw) ?: Date() } catch (e: Exception) { Date() }
        return SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(parsed)
    }

    fun displayTime(raw: String): String {
        val parsed = try { dateTimeFormat.parse(raw) ?: Date() } catch (e: Exception) { Date() }
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(parsed)
    }

    fun displayDateTime(raw: String): String = "${displayDate(raw)} · ${displayTime(raw)}"

    fun displayLineDate(raw: String): String {
        val parsed = try { dateTimeFormat.parse(raw) ?: Date() } catch (e: Exception) { Date() }
        return SimpleDateFormat("MMM d", Locale.getDefault()).format(parsed)
    }
}

object ReferenceGenerator {

    private const val ALPHABET = "0123456789ABCDEF"

    fun shortHex(): String = buildString {
        val random = java.security.SecureRandom()
        repeat(8) {
            append(ALPHABET[random.nextInt(ALPHABET.length)])
        }
    }

    fun forService(servicePrefix: String): String = "CAT-$servicePrefix-${shortHex()}"

    fun appointment(): String = "CAT-APT-${
        buildString {
            val random = java.security.SecureRandom()
            repeat(8) { append(ALPHABET[random.nextInt(ALPHABET.length)]) }
        }
    }"
}