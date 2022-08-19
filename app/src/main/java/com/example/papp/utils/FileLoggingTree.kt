package com.example.papp.utils

import android.content.Context
import android.util.Log
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import ch.qos.logback.core.util.StatusPrinter

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.example.papp.PSApplication

import timber.log.Timber
import java.nio.charset.Charset


class FileLoggingTree(context: Context) : Timber.DebugTree() {

    private val sharedPrefHandlerRef by lazy { PSApplication.settings }

    init {
        val logDirectory = "${context.getExternalFilesDir(null)}/logs"
        configureLogger(logDirectory)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FileLoggingTree::class.java)
        private const val LOG_PREFIX = "printerservice-log"
    }

    private fun configureLogger(logDirectory: String) {

        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerContext.reset()

        val rollingFileAppender = RollingFileAppender<ILoggingEvent>().apply {
            context = loggerContext
            isAppend = true
            file = "$logDirectory/$LOG_PREFIX-latest.log"
        }

        val fileNamingPolicy = SizeAndTimeBasedFNATP<ILoggingEvent>().apply {
            context = loggerContext
            setMaxFileSize(FileSize.valueOf("10MB"))
        }

        val rollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>().apply {
            context = loggerContext
            fileNamePattern = "$logDirectory/$LOG_PREFIX.%d{yyyy-MM-dd}.%i.log"
            maxHistory = sharedPrefHandlerRef.loggingSize / 10
            if (maxHistory < 2)
                maxHistory = 2
            setTotalSizeCap(FileSize(sharedPrefHandlerRef.loggingSize * FileSize.MB_COEFFICIENT))
            timeBasedFileNamingAndTriggeringPolicy = fileNamingPolicy
            setParent(rollingFileAppender)
            start()
        }

        val encoder = PatternLayoutEncoder().apply {
            context = loggerContext
            charset = Charset.forName("UTF-8")
            pattern = "%date %level [%thread] %msg%n"
            start()
        }

        rollingFileAppender.rollingPolicy = rollingPolicy
        rollingFileAppender.encoder = encoder
        rollingFileAppender.start()

        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
        root.level = Level.DEBUG
        root.addAppender(rollingFileAppender)

        StatusPrinter.print(loggerContext)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val logMessage = "$tag: $message"

        when (priority) {
            Log.VERBOSE -> Log.v(tag, message)
            Log.DEBUG   -> Log.d(tag, message)
            Log.INFO    -> Log.i(tag, message)
            Log.WARN    -> Log.w(tag, message)
            Log.ERROR   -> Log.e(tag, message)
        }

        when (priority) {
            Log.VERBOSE -> logger.trace(logMessage)
            Log.DEBUG   -> logger.debug(logMessage)
            Log.INFO    -> logger.info(logMessage)
            Log.WARN    -> logger.warn(logMessage)
            Log.ERROR   -> logger.error(logMessage)
        }
    }

}