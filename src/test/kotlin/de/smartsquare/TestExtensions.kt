package de.smartsquare

fun Any.resourceText(path: String) = javaClass.classLoader?.getResource(path)?.readText()
    ?: throw IllegalArgumentException("Resource not found: $path")
