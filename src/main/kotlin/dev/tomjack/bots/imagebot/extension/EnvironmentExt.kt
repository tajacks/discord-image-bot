package dev.tomjack.bots.imagebot.extension

import org.springframework.core.env.Environment

inline fun <reified T> Environment.required(key: String): T = this.getRequiredProperty(key, T::class.java)

inline fun <reified T> Environment.optional(key: String): T? = this.getProperty(key, T::class.java)
