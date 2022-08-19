package com.example.papp

import android.util.Base64
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

object MakeLabel {
    external fun add(x: Long, y: Long): Long
    external fun printn(x: String?)
    external fun printext(x: String?): String?
    external fun CreateTemplateEnv(): Long
    external fun DeleteTemplateEnv(descr: Long)
    external fun LoadLabelsData(descr: Long, data: String?): Long
    external fun SetTemplate(descr: Long, template: String?): String?
    external fun SetProperty(descr: Long, speed: Long, density: Long): String?
    external fun CreateLabel(
        descr: Long,
        printer_model: String?,
        dpi: Long,
        lang_type: String?,
        dialect: String?,
        label_num: Long
    ): Long

    external fun GetCreatedLabel(descr: Long): String
    external fun FreeCreatedLabel(p: String?)
    external fun ConvertTemplate(descr: Long, template: String?): String?
    external fun LoadPublic(descr: Long, public_key: String?): String?
    @Throws(IOException::class)
    fun readFromInputStream(file: File?): String {
        val resultStringBuilder = StringBuilder()
        BufferedReader(FileReader(file)).use { br ->
            var line: String?
            while (br.readLine().also { line = it } != null) {
                resultStringBuilder.append(line).append("\n")
            }
        }
        return resultStringBuilder.toString()
    }


    private const val path = "/sdcard/Templates/"

    @Throws(IOException::class)
    fun printTemplate(): ByteArray {
        try {
            val env = CreateTemplateEnv()
            val publicKeyData = readFromInputStream(File(path + "public"))
            val templateData = readFromInputStream(File(path + "template_signed.tmpl"))
            val labelData = readFromInputStream(File(path + "test.json"))
            LoadPublic(env, publicKeyData)
            SetTemplate(env, templateData)
            SetProperty(env, 1L, 5L)
            LoadLabelsData(env, labelData)
            CreateLabel(env, "XP-P323B", 203L, "TSPL", "Short", 0L)
            val label = GetCreatedLabel(env)
            val decodedBytes = Base64.decode(label, Base64.DEFAULT)
            val decodedString = String(decodedBytes)
            println(decodedString)
            return decodedBytes
        } catch (e: Exception) {
            Timber.i("Не найден шаблон")
            return byteArrayOf()
        }
    }

    init {
        System.loadLibrary("MakeLabel")
    }
}