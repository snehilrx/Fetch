package com.otaku.fetch

import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
class MainActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() = hiltRule.inject()

    @Test
    fun testActivityStates(){
        ActivityScenario.launch(MainActivity::class.java).use {
            it.onActivity { activity ->
                Toast.makeText(activity, "Launched Anything", Toast.LENGTH_SHORT).show()
            }
            it.moveToState(Lifecycle.State.STARTED)
            it.moveToState(Lifecycle.State.CREATED)
            it.moveToState(Lifecycle.State.RESUMED)
            it.moveToState(Lifecycle.State.DESTROYED)
        }
    }

}