package compose.wvhs.wvhsapp.Utils

import com.liftric.kvault.KVault

actual fun provideKVault(): KVault {
    return KVault()
}