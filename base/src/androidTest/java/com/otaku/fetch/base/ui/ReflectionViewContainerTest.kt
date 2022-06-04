package com.otaku.fetch.base.ui

import android.content.Context
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReflectionViewContainerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun testView() {
        val layout = LinearLayout(context)

        val reflection = ReflectionViewContainer(context)
        layout.addView(reflection)
    }

}