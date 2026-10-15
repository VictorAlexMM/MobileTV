package com.iptvplayer.app.core.network

import com.iptvplayer.app.core.common.AppException
import okhttp3.Interceptor
import okhttp3.Response

class OfflineModeInterceptor(private val gate: NetworkGate) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!gate.canAccessNetwork()) {
            throw AppException.OfflineModeException()
        }
        return chain.proceed(chain.request())
    }
}
