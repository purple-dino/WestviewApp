package compose.wvhs.wvhsapp

import com.liftric.kvault.KVault
import android.content.Context


lateinit var appContext: Context

fun initializeContext(context: Context) {
    appContext = context
}


actual fun provideKVault(): KVault {
    val context: Context = appContext
    return KVault(context)
}
