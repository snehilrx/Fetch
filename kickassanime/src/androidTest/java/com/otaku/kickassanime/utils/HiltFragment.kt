package com.otaku.kickassanime.utils

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import com.otaku.fetch.base.TestActivity

inline fun <reified T : Fragment> launchFragmentInContainer(
    noinline performTest: (activityScenario : ActivityScenario<TestActivity>) -> Unit
){
    launchAnythingInContainer(
        {
            T::class.java.classLoader?.let { fragmentClassLoader ->
                it.supportFragmentManager.beginTransaction().add(
                    it.supportFragmentManager.fragmentFactory
                        .instantiate(fragmentClassLoader, T::class.java.name),
                    "FRAGMENT UNDER TEST").commit()
                Toast.makeText(it, "Launched Fragment", Toast.LENGTH_SHORT).show()
            }
        },
        performTest
    )
}

fun launchAnythingInContainer(
    initContainer: (activity: TestActivity) -> Unit,
    performTest: (activityScenario : ActivityScenario<TestActivity>) -> Unit
){
    val activityScenario = ActivityScenario.launch(TestActivity::class.java)
    activityScenario.use { scenario ->
        scenario.onActivity {
            initContainer(it)
            Toast.makeText(it, "Launched Anything", Toast.LENGTH_SHORT).show()
        }
        performTest(activityScenario)
    }
}