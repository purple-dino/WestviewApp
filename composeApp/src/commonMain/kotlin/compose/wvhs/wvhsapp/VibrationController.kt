package compose.wvhs.wvhsapp


expect suspend fun vibrate(length: String)


interface ContextProvider {
    fun getApplicationContext(): Any
}
