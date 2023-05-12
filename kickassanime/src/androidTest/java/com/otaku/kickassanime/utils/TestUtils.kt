package com.otaku.kickassanime.utils

fun readFromFile(fileName: String)  = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName)?.bufferedReader()