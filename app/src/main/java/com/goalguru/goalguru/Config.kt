package com.goalguru.goalguru

object Config {
    val DEEPSEEK_API_KEY: String = System.getenv("DEEPSEEK_API_KEY") ?: ""
}
