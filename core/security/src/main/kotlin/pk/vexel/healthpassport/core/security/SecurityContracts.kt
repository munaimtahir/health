package pk.vexel.healthpassport.core.security

interface KeyStoreGateway {
    fun isAvailable(): Boolean
}

interface AppLockGateway {
    fun isLockEnabled(): Boolean
}

