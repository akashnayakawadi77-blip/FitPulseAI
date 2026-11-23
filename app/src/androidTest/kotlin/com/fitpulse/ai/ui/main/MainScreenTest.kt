package com.fitpulse.ai.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.fitpulse.ai.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { MainScreen() }
  }

  @Test
  fun bottomNavigation_tabsExist() {
    composeTestRule.onNodeWithText("Home").assertExists()
    composeTestRule.onNodeWithText("Workout").assertExists()
    composeTestRule.onNodeWithText("Nutrition").assertExists()
    composeTestRule.onNodeWithText("Coach").assertExists()
    composeTestRule.onNodeWithText("Progress").assertExists()
  }
}
