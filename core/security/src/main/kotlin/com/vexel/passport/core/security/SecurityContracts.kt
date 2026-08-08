package com.vexel.passport.core.security

interface KeyStoreGateway {
    fun isAvailable(): Boolean
}

interface AppLockGateway {
    fun isLockEnabled(): Boolean
}

