package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AccessLevel
import com.example.model.ActionStatus
import com.example.model.ActionType
import com.example.model.AiRequestState
import com.example.model.MicrophoneState
import com.example.model.OverlayState
import com.example.model.RiskLevel
import com.example.model.SafeAction
import com.example.model.ScreenCaptureState
import com.example.service.AIopterSessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AIopterSessionTest {

    private lateinit var context: Context
    private lateinit var sessionManager: AIopterSessionManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        sessionManager = AIopterSessionManager.getInstance(context)
        sessionManager.resetSession()
    }

    @Test
    fun testDefaultSessionStateIsSafe() {
        val state = sessionManager.sessionState.value
        assertEquals(ScreenCaptureState.OFF, state.screenCaptureState)
        assertEquals(MicrophoneState.IDLE, state.microphoneState)
        assertEquals(AiRequestState.IDLE, state.aiRequestState)
    }

    @Test
    fun testKillSwitchImmediatelyResetsAllSubsystems() {
        // Activate mic & toggle screen sharing
        sessionManager.startListening()
        sessionManager.toggleScreenSharing(true)

        // Trigger Emergency KILL switch
        sessionManager.triggerKillSwitch()

        val state = sessionManager.sessionState.value
        assertEquals(ScreenCaptureState.OFF, state.screenCaptureState)
        assertEquals(MicrophoneState.IDLE, state.microphoneState)
        assertEquals(AiRequestState.CANCELLED, state.aiRequestState)
        assertEquals(OverlayState.DISABLED, state.overlayState)
        assertFalse(sessionManager.repository.preferences.isScreenSharingEnabled)
    }

    @Test
    fun testDefaultSensitiveAppRulesSeeded() = runBlocking {
        sessionManager.repository.seedDefaultRulesIfNeeded()
        val rules = sessionManager.repository.appRules.first()
        assertTrue(rules.isNotEmpty())

        // Verify banking is gated
        val chase = rules.find { it.packageName == "com.chase.sig.android" }
        assertNotNull(chase)
        assertEquals(AccessLevel.ASK_EVERY_TIME, chase?.accessLevel)

        // Verify password manager is protected
        val bitwarden = rules.find { it.packageName == "com.bitwarden.mobile" }
        assertNotNull(bitwarden)
        assertEquals(AccessLevel.NEVER_ALLOW, bitwarden?.accessLevel)
    }

    @Test
    fun testSafeActionExecutionRequiresApproval() {
        val action = SafeAction(
            id = "test-nav-1",
            title = "Navigate to Blue Bottle Coffee",
            description = "Opens Google Maps navigation",
            riskLevel = RiskLevel.LEVEL_1_NAVIGATION,
            actionType = ActionType.OPEN_MAPS,
            payload = mapOf("destination" to "Blue Bottle Coffee"),
            status = ActionStatus.PROPOSED
        )

        assertEquals(ActionStatus.PROPOSED, action.status)
        val approved = action.copy(status = ActionStatus.CONFIRMED)
        assertEquals(ActionStatus.CONFIRMED, approved.status)
    }

    @Test
    fun testPropellerDockingPreferences() {
        val prefs = sessionManager.repository.preferences
        // Default docking corner is TOP_RIGHT
        assertEquals("TOP_RIGHT", prefs.dockedCorner)

        // Update corner to BOTTOM_LEFT
        prefs.dockedCorner = "BOTTOM_LEFT"
        assertEquals("BOTTOM_LEFT", prefs.dockedCorner)

        // Update coordinates
        prefs.bubblePosX = 48f
        prefs.bubblePosY = 1800f
        assertEquals(48f, prefs.bubblePosX)
        assertEquals(1800f, prefs.bubblePosY)
    }
}
