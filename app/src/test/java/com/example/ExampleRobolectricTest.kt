package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CampusSpace AI", appName)
  }

  @Test
  fun `verify repository loads all datasets without crashing`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val repo = com.example.data.CampusSpaceRepository(context)
    val audit = repo.loadDatasets()
    org.junit.Assert.assertEquals(120, audit.roomsCount)
    org.junit.Assert.assertTrue(audit.classScheduleCount > 0)
    org.junit.Assert.assertTrue(audit.roomRequestsCount > 0)
    org.junit.Assert.assertTrue(audit.eventsCount > 0)
  }

  @Test
  fun `verify ViewModel initializes cleanly without exception`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.CampusSpaceViewModel(context)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertNotNull(vm.uiState.value)
    org.junit.Assert.assertNotNull(vm.uiState.value.selectedTab)
  }



}
