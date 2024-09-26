package com.app.imagetotext

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.reflect.Method
import org.mockito.Mockito.*


class MainActivityTest {

    private lateinit var mainActivity: MainActivity
    private lateinit var contentResolver: ContentResolver

    @Before
    fun setUp() {
        contentResolver = mock(ContentResolver::class.java)

        // Use reflection to modify the val
        val fieldContentResolver = MainActivity::class.java.getDeclaredField("contentResolver")
        fieldContentResolver.isAccessible = true
        fieldContentResolver.set(mainActivity, contentResolver)

        // Use reflection to modify the val
        val fieldCacheDir = MainActivity::class.java.getDeclaredField("cacheDir")
        fieldCacheDir.isAccessible = true
        fieldCacheDir.set(mainActivity, ApplicationProvider.getApplicationContext<Context>().cacheDir)
    }

    @Test
    fun `test getFileFromUri returns file when successful`() {
        val uri = mock(Uri::class.java)
        val inputStream = mock(InputStream::class.java)
        val outputStream = mock(FileOutputStream::class.java)

        // Mocking behavior
        `when`(contentResolver.openInputStream(uri)).thenReturn(inputStream)

        val getFileNameMethod = MainActivity::class.java.getDeclaredMethod("getFileNameFromUri", Uri::class.java)
        getFileNameMethod.isAccessible = true

        `when`(getFileNameMethod.invoke(mainActivity, uri)).thenReturn("testFile.txt")
        `when`(FileOutputStream(any(File::class.java))).thenReturn(outputStream)

        // Call the private method via reflection
        val method: Method = MainActivity::class.java.getDeclaredMethod("getFileFromUri", Uri::class.java)
        method.isAccessible = true
        val result = method.invoke(mainActivity, uri) as? File

        // Validate the results
        assertNotNull(result)
        assertEquals("testFile.txt", result?.name)

        // Verify interactions
        verify(inputStream).copyTo(outputStream)
        verify(inputStream).close()
        verify(outputStream).close()
    }

    @Test
    fun `test getFileFromUri returns null when input stream is null`() {
        val uri = mock(Uri::class.java)

        `when`(contentResolver.openInputStream(uri)).thenReturn(null)

        val method: Method = MainActivity::class.java.getDeclaredMethod("getFileFromUri", Uri::class.java)
        method.isAccessible = true
        val result = method.invoke(mainActivity, uri)

        assertNull(result)
    }

    @Test
    fun `test getFileFromUri returns null when file name is null`() {
        val uri = mock(Uri::class.java)
        val inputStream = mock(InputStream::class.java)

        `when`(contentResolver.openInputStream(uri)).thenReturn(inputStream)

        val getFileNameMethod: Method = MainActivity::class.java.getDeclaredMethod("getFileFromUri", Uri::class.java)
        getFileNameMethod.isAccessible = true
        `when`(getFileNameMethod.invoke(mainActivity, uri)).thenReturn(null)

        val method: Method = MainActivity::class.java.getDeclaredMethod("getFileFromUri", Uri::class.java)
        method.isAccessible = true
        val result = method.invoke(mainActivity, uri)

        assertNull(result)
    }

    @Test
    fun `test getFileFromUri handles exception and returns null`() {
        val uri = mock(Uri::class.java)

        `when`(contentResolver.openInputStream(uri)).thenThrow(RuntimeException("Error"))

        val getFileNameMethod: Method = MainActivity::class.java.getDeclaredMethod("getFileFromUri", Uri::class.java)
        getFileNameMethod.isAccessible = true
        val result = getFileNameMethod.invoke(mainActivity, uri)

        assertNull(result)
    }
}
