package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class WebSearchResult(
    val title: String,
    val snippet: String,
    val sourceUrl: String
)

object WebSearchEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .build()

    suspend fun searchLiveWeb(query: String): List<WebSearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<WebSearchResult>()
        try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")

            // 1. Query DuckDuckGo Instant Answer API
            val ddgUrl = "https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1&skip_disambig=1"
            val ddgRequest = Request.Builder()
                .url(ddgUrl)
                .header("User-Agent", "BidiokAI-Android/1.0")
                .build()

            val ddgResponse = client.newCall(ddgRequest).execute()
            if (ddgResponse.isSuccessful) {
                val bodyStr = ddgResponse.body?.string() ?: ""
                if (bodyStr.isNotBlank()) {
                    val json = JSONObject(bodyStr)
                    val abstractText = json.optString("AbstractText", "")
                    val heading = json.optString("Heading", query)
                    val url = json.optString("AbstractURL", "https://duckduckgo.com/?q=$encodedQuery")

                    if (abstractText.isNotBlank()) {
                        results.add(
                            WebSearchResult(
                                title = heading,
                                snippet = abstractText,
                                sourceUrl = url
                            )
                        )
                    }
                }
            }

            // 2. Query Wikipedia Summary API for knowledge grounding
            val wikiUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/$encodedQuery"
            val wikiRequest = Request.Builder()
                .url(wikiUrl)
                .header("User-Agent", "BidiokAI-Assistant/1.0 (contact@bidiok.ai)")
                .build()

            val wikiResponse = client.newCall(wikiRequest).execute()
            if (wikiResponse.isSuccessful) {
                val wikiBody = wikiResponse.body?.string() ?: ""
                if (wikiBody.isNotBlank()) {
                    val wikiJson = JSONObject(wikiBody)
                    val extract = wikiJson.optString("extract", "")
                    val title = wikiJson.optString("title", query)
                    val pageUrl = wikiJson.optJSONObject("content_urls")
                        ?.optJSONObject("desktop")
                        ?.optString("page", "https://en.wikipedia.org/wiki/$encodedQuery")
                        ?: "https://en.wikipedia.org/wiki/$encodedQuery"

                    if (extract.isNotBlank() && extract != results.firstOrNull()?.snippet) {
                        results.add(
                            WebSearchResult(
                                title = title,
                                snippet = extract,
                                sourceUrl = pageUrl
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fall back to cached / neural synthesis if offline
        }

        results
    }
}
