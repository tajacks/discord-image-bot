package dev.tomjack.bots.imagebot.extension

import org.springframework.core.env.Environment

inline fun <reified T> Environment.required(key: String): T = this.getRequiredProperty(key, T::class.java)
