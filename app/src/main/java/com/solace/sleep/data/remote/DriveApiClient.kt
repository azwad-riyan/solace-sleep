package com.solace.sleep.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveApiClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    companion object {
        private const val DRIVE_API_FILES = "https://www.googleapis.com/drive/v3/files"
        private const val DRIVE_UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files"
        private const val FOLDER_MIME = "application/vnd.google-apps.folder"
        private const val JSON_MIME = "application/json"
        private const val FOLDER_NAME = "SolaceSleepBackup"
    }

    suspend fun uploadBackup(
        authToken: String,
        syncDto: SyncDto,
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val folderId = getOrCreateFolder(authToken)
            val jsonContent = json.encodeToString(syncDto)
            val fileId = uploadJsonFile(authToken, folderId, fileName, jsonContent)
            Result.success(fileId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload backup to Drive")
            Result.failure(e)
        }
    }

    suspend fun downloadLatestBackup(authToken: String): Result<SyncDto?> = withContext(Dispatchers.IO) {
        try {
            val folderId = getOrCreateFolder(authToken)
            val fileId = findLatestBackupFile(authToken, folderId)
                ?: return@withContext Result.success(null)
            val content = downloadFileContent(authToken, fileId)
            val syncDto = json.decodeFromString<SyncDto>(content)
            Result.success(syncDto)
        } catch (e: Exception) {
            Timber.e(e, "Failed to download backup from Drive")
            Result.failure(e)
        }
    }

    private fun getOrCreateFolder(authToken: String): String {
        val searchUrl = URL(
            "$DRIVE_API_FILES?q=name='$FOLDER_NAME' and mimeType='$FOLDER_MIME' and trashed=false" +
                    "&fields=files(id,name)"
        )
        val response = makeGetRequest(authToken, searchUrl)
        val filesJson = Json.parseToJsonElement(response)
        val files = filesJson.jsonObject["files"]?.jsonArray
        if (!files.isNullOrEmpty()) {
            return files[0].jsonObject["id"]?.jsonPrimitive?.content ?: createFolder(authToken)
        }
        return createFolder(authToken)
    }

    private fun createFolder(authToken: String): String {
        val url = URL(DRIVE_API_FILES)
        val body = """{"name": "$FOLDER_NAME", "mimeType": "$FOLDER_MIME"}"""
        val response = makePostRequest(authToken, url, body, JSON_MIME)
        val responseJson = Json.parseToJsonElement(response)
        return responseJson.jsonObject["id"]?.jsonPrimitive?.content
            ?: throw IOException("Failed to create Drive folder")
    }

    private fun findLatestBackupFile(authToken: String, folderId: String): String? {
        val query = "'$folderId' in parents and name contains 'solace_backup' and trashed=false"
        val url = URL("$DRIVE_API_FILES?q=${query.encodeUrl()}&orderBy=createdTime desc&fields=files(id,name)&pageSize=1")
        val response = makeGetRequest(authToken, url)
        val filesJson = Json.parseToJsonElement(response)
        val files = filesJson.jsonObject["files"]?.jsonArray
        return if (!files.isNullOrEmpty()) {
            files[0].jsonObject["id"]?.jsonPrimitive?.content
        } else null
    }

    private fun uploadJsonFile(
        authToken: String,
        folderId: String,
        fileName: String,
        content: String
    ): String {
        val metadata = """{"name": "$fileName", "parents": ["$folderId"]}"""
        val boundary = "solace_multipart_boundary"
        val body = "--$boundary\r\nContent-Type: $JSON_MIME\r\n\r\n$metadata\r\n" +
                "--$boundary\r\nContent-Type: $JSON_MIME\r\n\r\n$content\r\n--$boundary--"
        val url = URL("$DRIVE_UPLOAD_URL?uploadType=multipart")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $authToken")
        conn.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
        conn.doOutput = true
        conn.outputStream.write(body.toByteArray())
        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        val responseJson = Json.parseToJsonElement(response)
        return responseJson.jsonObject["id"]?.jsonPrimitive?.content
            ?: throw IOException("Failed to upload file")
    }

    private fun downloadFileContent(authToken: String, fileId: String): String {
        val url = URL("$DRIVE_API_FILES/$fileId?alt=media")
        return makeGetRequest(authToken, url)
    }

    private fun makeGetRequest(authToken: String, url: URL): String {
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $authToken")
        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        return response
    }

    private fun makePostRequest(authToken: String, url: URL, body: String, contentType: String): String {
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $authToken")
        conn.setRequestProperty("Content-Type", contentType)
        conn.doOutput = true
        conn.outputStream.write(body.toByteArray())
        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        return response
    }

    private fun String.encodeUrl(): String = java.net.URLEncoder.encode(this, "UTF-8")
}
