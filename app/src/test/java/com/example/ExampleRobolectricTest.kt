package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.BidiokAiEngine
import com.example.ai.ThreeDGenerator
import com.example.data.local.AppDatabase
import com.example.data.model.AiModelType
import com.example.data.model.SearchHistoryRecord
import com.example.data.model.UserMemoryRecord
import com.example.qr.QrCodeHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Bidiok AI", appName)
  }

  @Test
  fun `qr code detector identifies formats properly`() {
    val urlType = QrCodeHelper.detectQrType("https://bidiok.ai")
    assertEquals("URL", urlType)

    val wifiType = QrCodeHelper.detectQrType("WIFI:S:MyMesh;P:12345678;;")
    assertEquals("WIFI", wifiType)

    val textType = QrCodeHelper.detectQrType("Hello Bidiok AI")
    assertEquals("TEXT", textType)
  }

  @Test
  fun `three d generator synthesizes valid mesh geometry`() {
    val quantumCore = ThreeDGenerator.getQuantumCoreMesh()
    assertTrue(quantumCore.vertices.isNotEmpty())
    assertTrue(quantumCore.edges.isNotEmpty())
    assertTrue(quantumCore.faces.isNotEmpty())
    assertNotNull(quantumCore.objCode)

    val droneMesh = ThreeDGenerator.getCyberDroneMesh()
    assertTrue(droneMesh.vertices.size > 10)
  }

  @Test
  fun `search history database operations work properly`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getInstance(context)
    val dao = db.searchHistoryDao()

    dao.clearAllHistory()
    val id = dao.insertHistory(
      SearchHistoryRecord(query = "Quantum 3D Drone", category = "3D_STUDIO")
    )
    assertTrue(id > 0)

    val history = dao.getAllHistory().first()
    assertEquals(1, history.size)
    assertEquals("Quantum 3D Drone", history.first().query)

    dao.deleteById(history.first().id)
    val updated = dao.getAllHistory().first()
    assertTrue(updated.isEmpty())
  }

  @Test
  fun `user memory storage and recall operations work properly`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getInstance(context)
    val memoryDao = db.userMemoryDao()

    memoryDao.clearAllMemories()
    val memId = memoryDao.insertMemory(
      UserMemoryRecord(keyTopic = "Project", content = "User is building Bidiok Quantum AI")
    )
    assertTrue(memId > 0)

    val memories = memoryDao.getAllMemoriesList()
    assertEquals(1, memories.size)
    assertEquals("Project", memories.first().keyTopic)

    // Verify AI recalls this memory when asked
    val recallResponse = BidiokAiEngine.processQuery(
      prompt = "What do you know about me?",
      modelType = AiModelType.BIDIOK_QUANTUM,
      deepReasoningEnabled = false,
      webSearchEnabled = false,
      userMemories = memories
    )
    assertTrue(recallResponse.content.contains("Bidiok Quantum AI") || recallResponse.content.contains("Project"))
  }

  @Test
  fun `bidiok ai responds in user requested language`() = runBlocking {
    val bengaliResponse = BidiokAiEngine.processQuery(
      prompt = "কেমন আছো?",
      modelType = AiModelType.BIDIOK_QUANTUM,
      deepReasoningEnabled = false,
      webSearchEnabled = false
    )
    assertEquals("bengali", bengaliResponse.languageCode)
    assertTrue(bengaliResponse.content.contains("বিডিওক এআই") || bengaliResponse.content.contains("Bidiok AI"))

    val englishResponse = BidiokAiEngine.processQuery(
      prompt = "Explain quantum entanglement",
      modelType = AiModelType.BIDIOK_QUANTUM,
      deepReasoningEnabled = false,
      webSearchEnabled = false
    )
    assertEquals("english", englishResponse.languageCode)
  }
}
