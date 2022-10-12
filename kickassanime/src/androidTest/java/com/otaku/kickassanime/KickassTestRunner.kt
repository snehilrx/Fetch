package com.otaku.kickassanime

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication


class KickassTestRunner : AndroidJUnitRunner() {
    @Throws(ClassNotFoundException::class, IllegalAccessException::class, InstantiationException::class)
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}