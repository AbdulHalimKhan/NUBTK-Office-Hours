package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun verify_buildConfig_versionMetadata() {
        assertEquals("com.aistudio.nubtkofficehours.qvnf", BuildConfig.APPLICATION_ID)
        assertEquals(4, BuildConfig.VERSION_CODE)
        assertEquals("1.3", BuildConfig.VERSION_NAME)
        assertTrue(BuildConfig.VERSION_NAME.isNotBlank())
    }
}
