package com.example.data.service

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

object FileConverterEngine {

    enum class ConversionType(val label: String, val inputFormat: String, val outputFormat: String) {
        TEXT_TO_PDF("Text / Notes to PDF", "TXT / MD", "PDF"),
        IMAGE_TO_PDF("Photo / Image to PDF", "JPG / PNG", "PDF"),
        JSON_TO_CSV("JSON Data to CSV Spreadsheet", "JSON", "CSV"),
        CSV_TO_JSON("CSV Table to JSON Array", "CSV", "JSON"),
        MARKDOWN_TO_HTML("Markdown to HTML Document", "MD", "HTML")
    }

    /**
     * Reads text content from a URI (text, json, csv, code files).
     */
    fun readTextFromUri(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: ""
        } catch (e: Exception) {
            "Unable to read file content: ${e.localizedMessage}"
        }
    }

    /**
     * Converts raw text to PDF file
     */
    fun convertTextToPdf(context: Context, title: String, content: String): File {
        return PdfGeneratorEngine.createDocumentPdf(context, title, "Bidiok Universal Converter", content)
    }

    /**
     * Converts Image URI to PDF
     */
    fun convertImageToPdf(context: Context, imageUri: Uri, title: String): File? {
        return try {
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    PdfGeneratorEngine.createImagePdf(context, bitmap, title)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Converts JSON array of objects to CSV formatted text
     */
    fun convertJsonToCsv(jsonString: String): String {
        return try {
            val trimmed = jsonString.trim()
            val jsonArray = if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                // look for array inside object
                val keys = obj.keys()
                var foundArray: JSONArray? = null
                while (keys.hasNext()) {
                    val key = keys.next()
                    val opt = obj.optJSONArray(key)
                    if (opt != null) {
                        foundArray = opt
                        break
                    }
                }
                foundArray ?: JSONArray().put(obj)
            } else {
                return "Error: Input must be valid JSON format."
            }

            if (jsonArray.length() == 0) return "Empty JSON array."

            // Collect all unique keys
            val allHeaders = mutableSetOf<String>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue
                val iter = item.keys()
                while (iter.hasNext()) {
                    allHeaders.add(iter.next())
                }
            }

            val headerList = allHeaders.toList()
            val sb = StringBuilder()
            sb.append(headerList.joinToString(",") { escapeCsv(it) }).append("\n")

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue
                val row = headerList.map { key ->
                    escapeCsv(item.optString(key, ""))
                }
                sb.append(row.joinToString(",")).append("\n")
            }

            sb.toString()
        } catch (e: Exception) {
            "JSON to CSV Conversion error: ${e.message}"
        }
    }

    /**
     * Converts CSV text to pretty formatted JSON array
     */
    fun convertCsvToJson(csvString: String): String {
        return try {
            val lines = csvString.trim().split("\n").filter { it.isNotBlank() }
            if (lines.isEmpty()) return "[]"

            val headers = parseCsvLine(lines[0])
            val jsonArray = JSONArray()

            for (i in 1 until lines.size) {
                val values = parseCsvLine(lines[i])
                val jsonObject = JSONObject()
                headers.forEachIndexed { index, header ->
                    val value = if (index < values.size) values[index] else ""
                    jsonObject.put(header, value)
                }
                jsonArray.put(jsonObject)
            }

            jsonArray.toString(2)
        } catch (e: Exception) {
            "CSV to JSON error: ${e.message}"
        }
    }

    /**
     * Simple Markdown to HTML
     */
    fun convertMarkdownToHtml(markdown: String): String {
        val lines = markdown.split("\n")
        val html = StringBuilder("<!DOCTYPE html><html><head><meta charset='utf-8'><title>Bidiok Export</title><style>body{font-family:sans-serif;padding:24px;background:#0d1117;color:#e6edf3;}h1,h2,h3{color:#58a6ff;}pre{background:#161b22;padding:12px;border-radius:8px;}code{color:#f0883e;}</style></head><body>")

        for (line in lines) {
            when {
                line.startsWith("### ") -> html.append("<h3>").append(line.removePrefix("### ")).append("</h3>")
                line.startsWith("## ") -> html.append("<h2>").append(line.removePrefix("## ")).append("</h2>")
                line.startsWith("# ") -> html.append("<h1>").append(line.removePrefix("# ")).append("</h1>")
                line.startsWith("- ") -> html.append("<li>").append(line.removePrefix("- ")).append("</li>")
                line.isBlank() -> html.append("<br>")
                else -> html.append("<p>").append(line).append("</p>")
            }
        }
        html.append("</body></html>")
        return html.toString()
    }

    /**
     * Saves text to a local cache file with given extension
     */
    fun saveTextToFile(context: Context, fileName: String, extension: String, text: String): File {
        val file = File(context.cacheDir, "$fileName.$extension")
        FileOutputStream(file).use { out ->
            out.write(text.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var inQuotes = false
        val current = StringBuilder()
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    current.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(current.toString().trim())
                current.clear()
            } else {
                current.append(c)
            }
            i++
        }
        tokens.add(current.toString().trim())
        return tokens
    }
}
