package com.atafelska.service.notes.core

import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Used to enrich class with logging capabilities
 */
interface Logging

/**
 * Returns a logger for a class
 */
fun <T : Logging> T.logger(): Logger = LoggerFactory.getLogger(javaClass)
