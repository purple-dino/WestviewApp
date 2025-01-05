package compose.wvhs.wvhsapp.Utils


expect suspend fun vibrate(length: String)


interface ContextProvider {
    fun getApplicationContext(): Any
}
