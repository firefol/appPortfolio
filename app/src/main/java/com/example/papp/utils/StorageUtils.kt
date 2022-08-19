package com.example.papp.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.annotation.RequiresApi
import com.example.papp.PSApplication
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.collections.HashMap

/**
 * Функции для работы с хранилищем
 */
object StorageUtils {

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    val logAbsPath = "${PSApplication.instance.getExternalFilesDir(null)}/logs"
    @RequiresApi(Build.VERSION_CODES.N)
    val setAbsPath = "${PSApplication.instance.dataDir}/shared_prefs"

    /**
     * Возвращает размер каталога с файлами
     * @param dirPath Путь для подсчета
     * @return Суммарный размер всех файлов в каталоге. Рекурсивно вложенные каталоги не участвуют в подсчете
     */
    private fun getDirFilesSize(dirPath: String?): Double {
        var totalSize = 0.0

        if (dirPath == null) {
            return totalSize
        }

        val d = File(dirPath)

        if (!d.exists() || !d.isDirectory) {
            return totalSize
        }

        for (f in d.listFiles()) {
            if (f.isFile) {
                totalSize += f.length().toDouble()
            }
        }

        return totalSize
    }

    /**
     * Размер логов
     * @return Строка, содержащая размер логов с указанием единиц измерения (B, KB, MB, GB, TB)
     */
    fun getLogsSize(): String {
        return sizeConverter(getDirFilesSize(logAbsPath))
    }


    /**
     * Занято на устройстве
     * @return Строка, содержащая общий размер всех файлов с указанием единиц измерения (B, KB, MB, GB, TB)
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getOverallSize(): String {
        return sizeConverter(getOccupiedSpace())
    }

    /**
     * Свободное пространство
     * @return Строка, содержащая свободное место на диске с указанием единиц измерения (B, KB, MB, GB, TB)
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getFreeSize(): String {
        return sizeConverter(getAvailableExternalMemorySize().toDouble())
    }

    /**
     * Процент свободного пространства
     * @return Доля свободного месте на диске от общего размера в %
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getAvailableSpacePercent(): Double {
        val availableExternalMemory = getAvailableExternalMemorySize().toDouble()
        val totalExternalMemory = getTotalExternalMemorySize()

        return availableExternalMemory / (totalExternalMemory / 100)
    }

    /**
     * Занятое файлами пространство
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun getOccupiedSpace(): Double {
        val availableExternalMemory = getAvailableExternalMemorySize().toDouble()
        val totalExternalMemory = getTotalExternalMemorySize()

        return totalExternalMemory - availableExternalMemory
    }

    /**
     * Доступность внешнего хранилища
     */
    private fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Доступное пространство на внешнем хранилище в байтах
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun getAvailableExternalMemorySize(): Long {
        if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong

            return availableBlocks * blockSize
        }

        return 0
    }

    /**
     * Общий размер
     * @return Строка, содержащая общий размер диска с указанием единиц измерения (B, KB, MB, GB, TB)
     */
    fun getTotalSize(): String? {
        return sizeConverter(getTotalExternalMemorySize().toDouble())
    }

    /**
     * Свободное пространство на внешнем хранилище в байтах
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun getTotalExternalMemorySize(): Long {
        return if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong

            totalBlocks * blockSize
        } else {
            0
        }
    }

    /**
     * Конвертирует численный размер в строку с единицами измерения.
     * Используется для конвертации длинных чисел в наглядный вид, число преобразуется в более крупные единицы (B->KB->MB->GB->TB).
     * @param n Число для конвертации в заданных единицах
     * @param c Единицы для конвертации (1=B; 2=KB; 3=MB; 4=GB; 5=TB). По умолчанию 1.
     * @return Строка, содержащая размер с единицами измерения.
     */
    private fun sizeConverter(n: Double, c: Int = 1): String {
        if (n > 1024 && c <= 5) {
            return sizeConverter(n / 1024, c + 1)
        } else {
            var sizeStr = String.format("%.2f", n)

            when {
                c == 1 -> {
                    sizeStr += "  B"
                }
                c == 2 -> {
                    sizeStr += " KB"
                }
                c == 3 -> {
                    sizeStr += " MB"
                }
                c == 4 -> {
                    sizeStr += " GB"
                }
                c >= 5 -> {
                    sizeStr += " TB"
                }
            }


            return sizeStr
        }
    }

    /**
     * Удаление файлов лога, дата изменений которых превышает 7 дней
     */
    fun deleteAppLogs(): Boolean {
        Timber.d("Удаление файлов лога")
        var deleted = false;
        val d = File(logAbsPath)

        if (d.exists() && d.isDirectory) {
            for (f in d.listFiles()!!) {
                if (f.isFile) {
                    val diff: Long = Date().getTime() - f.lastModified()
                    if (diff > 7 * 24 * 60 * 60 * 1000) {
                        Timber.d("Удалить $f" )
                        f.delete()
                        deleted = true
                    }
                }
            }
        }
        Timber.d("Удаление файлов лога - конец")
        return deleted
    }

    /**
     * Экспорт файлов лога и настроек в zip-архив в папку /Download
     * @return True, если экспорт прошел успешно. Или False, если нет.
     * @throws OutOfSpaceException в случае, если не удалось записать файл, т.к. закончилось место на диске
     * @throws java.io.IOException при других ошибках ввода-вывода
     * @throws Exception при остальных ошибках
     */
    fun exportDiagFile(context: Context): Boolean {
        Timber.d("Экспорт файлов логов")
        try {
            val srcFiles: MutableList<String> = mutableListOf(

            )

            val d = File(logAbsPath)
            if (d.exists() && d.isDirectory) {
                srcFiles.addAll(d.listFiles{ f -> f.isFile }!!.map{ it.path})
            }

            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault())
            val targetFile = File(Environment.getExternalStorageDirectory().path
                    + "/Download/"
                    +  "PrinterServiceDiag"
                    + dateFormat.format(Date())
                    + ".zip" )
            if (targetFile.exists()) {
                targetFile.delete()
            }


            val fos = FileOutputStream(targetFile)
            val zipOut = ZipOutputStream(fos)
            val folders = HashMap<String, String>()

            for (srcFile in srcFiles) {
                val fileToZip = File(srcFile)
                if (fileToZip.exists()) {
                    val fis = FileInputStream(fileToZip)

                    var folder = "";
                    if (fileToZip.parentFile != null) {
                        folder = fileToZip.parentFile!!.name
                        if (folders.get(folder) == null) {
                            folders.put(folder, "")
                            zipOut.putNextEntry(ZipEntry(folder + "/"))
                        }
                    }

                    val zipEntry = ZipEntry(folder + "/" + fileToZip.name)

                    Timber.d("Экспорт файла ${fileToZip.name}")


                    zipOut.putNextEntry(zipEntry)

                    val bytes = ByteArray(1024)
                    var length: Int

                    while (fis.read(bytes).also { length = it } >= 0) {
                        zipOut.write(bytes, 0, length)
                    }

                    fis.close()
                }
            }

            zipOut.close()
            fos.close()

            // Чтобы файл сразу был виден в проводнике через USB, нунжо его проиндексировать

            Timber.d("Экспорт файлов логов - конец: $targetFile")
            return true
        } catch (e: IOException) {
            Timber.e(e, "Не удалось экспортировать логи")

            // INFO: Код для определения нехватки места при экспорте БД

            return false;
        } catch (e: Exception) {
            Timber.e(e, "Не удалось экспортировать логи")
            return false
        }

    }

    /**
     * Индексирование файла MediaScanner'ом.
     * Файл виден в проводнике через USB-подключение только после того, как будет проиндексирован MediaScanner'ом.
     */

    fun writeTextFile(context: Context, fileName: String, body: String) {
        try {
            val file = File(context.filesDir, fileName)
            if (file.exists())
                file.delete()
            val writer = FileWriter(file)
            writer.append(body)
            writer.flush()
            writer.close()
        } catch (e: java.lang.Exception) {
            Timber.e(e, "Ошибка записи в файл: $fileName")
            throw e
        }
    }

    fun readTextFile(fileName: String): String? {
        try {
            val file = File(fileName)
            if (!file.exists())
                return null;

            val reader = FileReader (file)
            val body = reader.readText()
            reader.close()

            return body
        } catch (e: java.lang.Exception) {
            Timber.e(e, "Ошибка чтения файла: $fileName")
            return null
        }
    }


}



