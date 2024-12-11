package compose.wvhs.wvhsapp

import com.liftric.kvault.KVault

actual fun provideKVault(): KVault {
    return KVault()
}