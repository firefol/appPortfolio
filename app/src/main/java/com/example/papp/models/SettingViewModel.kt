package com.example.papp.models

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.papp.utils.Event
import com.example.papp.utils.StorageUtils
import timber.log.Timber

class SettingViewModel : ViewModel() {

    val dataLive = MutableLiveData<Diag>()
    val fetchError = MutableLiveData<Event<Exception>>()
    val fetchInfo = MutableLiveData<Event<String>>()

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun fetch(context: Context) = viewModelScope.launch {
        Timber.d("<= Загрузка")
        try {
            dataLive.value = withContext(Dispatchers.Default) {
                Diag(
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName,
                    StorageUtils.getLogsSize(),
                    StorageUtils.getFreeSize(),
                    StorageUtils.getOverallSize(),
                    StorageUtils.getAvailableSpacePercent(),
                    StorageUtils.getTotalSize()
                )
            }
            Timber.d("<= Конец Загрузки")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка загрузки")
            fetchError.value = Event(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun clearLogs(context: Context) {
        Timber.d("<= Очистка логов")
        try {
            if (StorageUtils.deleteAppLogs()) {
                fetch(context)
                fetchInfo.value = Event("Старые логи удалены")
            } else
                fetchInfo.value = Event("Нет старых логов для удаления")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка очистки логов")
            fetchError.value = Event(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun exportDiag(context: Context) {
        try {
            if (StorageUtils.exportDiagFile(context)) {
                fetch(context)
                fetchInfo.value = Event("Данные диагностики экспортированы в каталог Download")
            }
            else {
                fetchInfo.value = Event("Не удалось экспортировать данные диагностики")
            }
        } catch (e: Exception) {
            Timber.e(e, "Не удалось экспортировать данные диагностики")
            fetchError.value = Event(e)
        }
    }

    data class Diag(
        val version: String,
        val logSize: String,
        val freeSize: String,
        val overalSize: String,
        val freeSizePercent: Double,
        val totalSize: String?
    )

    companion object {
        const val NA = "N/A"
    }
}