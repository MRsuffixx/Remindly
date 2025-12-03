package com.mrsuffix.remindly.data.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection

data class AppVersion(
    val versionName: String,
    val versionCode: Int,
    val releaseNotes: String,
    val downloadUrl: String,
    val isPreRelease: Boolean
)

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: AppVersion?,
    val currentVersion: String
)

@Singleton
class UpdateChecker @Inject constructor() {
    
    companion object {
        const val GITHUB_REPO = "MRsuffixx/Remindly"
        // Use HTTPS for secure connection
        const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases"
        const val GITHUB_REPO_URL = "https://github.com/$GITHUB_REPO"
        const val CURRENT_VERSION = "1.0.0"
        // Version code calculated same way as parseVersionCode: 1*10000 + 0*100 + 0 = 10000
        const val CURRENT_VERSION_CODE = 10000
        
        // Security: Only allow downloads from official GitHub domain
        private val ALLOWED_DOWNLOAD_DOMAINS = listOf(
            "github.com",
            "api.github.com"
        )
    }
    
    /**
     * Check for updates from GitHub releases
     * Security: Uses HTTPS, validates response, sanitizes input
     */
    suspend fun checkForUpdates(includePreRelease: Boolean = false): UpdateInfo {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(GITHUB_API_URL)
                
                // Security: Ensure HTTPS connection
                if (url.protocol != "https") {
                    return@withContext UpdateInfo(
                        hasUpdate = false,
                        latestVersion = null,
                        currentVersion = CURRENT_VERSION
                    )
                }
                
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                // Security: Set User-Agent to identify the app
                connection.setRequestProperty("User-Agent", "Remindly-App/$CURRENT_VERSION")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                // Security: Don't follow redirects automatically (prevent redirect attacks)
                connection.instanceFollowRedirects = false
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    
                    // Security: Limit response size to prevent memory exhaustion
                    if (response.length > 1_000_000) { // 1MB max
                        return@withContext UpdateInfo(
                            hasUpdate = false,
                            latestVersion = null,
                            currentVersion = CURRENT_VERSION
                        )
                    }
                    
                    val releases = JSONArray(response)
                    
                    var latestRelease: AppVersion? = null
                    
                    for (i in 0 until minOf(releases.length(), 10)) { // Security: Limit iterations
                        val release = releases.getJSONObject(i)
                        val isPreRelease = release.optBoolean("prerelease", false)
                        
                        // Skip pre-releases if not requested
                        if (isPreRelease && !includePreRelease) continue
                        
                        val tagName = release.optString("tag_name", "")
                        if (tagName.isBlank()) continue
                        
                        // Security: Sanitize version name (only allow alphanumeric, dots, hyphens)
                        val versionName = tagName
                            .removePrefix("v")
                            .removePrefix("V")
                            .filter { it.isLetterOrDigit() || it == '.' || it == '-' }
                        
                        if (versionName.isBlank()) continue
                        
                        // Security: Sanitize release notes (limit length, remove potential script tags)
                        val body = release.optString("body", "")
                            .take(2000) // Limit to 2000 chars
                            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
                        
                        val htmlUrl = release.optString("html_url", "")
                        
                        // Security: Validate download URL domain
                        if (!isValidDownloadUrl(htmlUrl)) continue
                        
                        val versionCode = parseVersionCode(versionName)
                        
                        latestRelease = AppVersion(
                            versionName = versionName,
                            versionCode = versionCode,
                            releaseNotes = body,
                            downloadUrl = htmlUrl,
                            isPreRelease = isPreRelease
                        )
                        break // Get only the first (latest) matching release
                    }
                    
                    // Security: Strict version comparison
                    val hasUpdate = latestRelease != null && 
                        latestRelease.versionCode > CURRENT_VERSION_CODE
                    
                    UpdateInfo(
                        hasUpdate = hasUpdate,
                        latestVersion = latestRelease,
                        currentVersion = CURRENT_VERSION
                    )
                } else {
                    UpdateInfo(
                        hasUpdate = false,
                        latestVersion = null,
                        currentVersion = CURRENT_VERSION
                    )
                }
            } catch (e: Exception) {
                // Security: Don't expose exception details
                UpdateInfo(
                    hasUpdate = false,
                    latestVersion = null,
                    currentVersion = CURRENT_VERSION
                )
            }
        }
    }
    
    /**
     * Security: Validate that download URL is from allowed domains
     */
    private fun isValidDownloadUrl(urlString: String): Boolean {
        return try {
            if (urlString.isBlank()) return false
            val url = URL(urlString)
            // Must be HTTPS
            if (url.protocol != "https") return false
            // Must be from allowed domain
            ALLOWED_DOWNLOAD_DOMAINS.any { domain ->
                url.host == domain || url.host.endsWith(".$domain")
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Parse version string to version code
     * e.g., "1.0.0" -> 10000, "1.2.3" -> 10203, "2.0.0" -> 20000
     */
    private fun parseVersionCode(version: String): Int {
        return try {
            // Security: Only parse numeric parts
            val cleanVersion = version.filter { it.isDigit() || it == '.' }
            val parts = cleanVersion.split(".")
            val major = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 99) ?: 0
            val minor = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 99) ?: 0
            val patch = parts.getOrNull(2)?.toIntOrNull()?.coerceIn(0, 99) ?: 0
            major * 10000 + minor * 100 + patch
        } catch (e: Exception) {
            0
        }
    }
}
