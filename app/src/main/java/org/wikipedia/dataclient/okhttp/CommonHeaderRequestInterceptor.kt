package org.wikipedia.dataclient.okhttp

import okhttp3.Interceptor
import okhttp3.Response
import org.wikipedia.WikipediaApp
import org.wikipedia.dataclient.RestService
import org.wikipedia.settings.Prefs.isEventLoggingEnabled
import java.io.IOException

internal class CommonHeaderRequestInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val app = WikipediaApp.instance
        val builder = chain.request().newBuilder()
                .header("User-Agent", app.userAgent)
                .header(if (isEventLoggingEnabled) "X-WMF-UUID" else "DNT",
                        if (isEventLoggingEnabled) app.appInstallID else "1")
        if (chain.request().url.encodedPath.contains(RestService.PAGE_HTML_ENDPOINT)) {
            builder.header("Accept", RestService.ACCEPT_HEADER_MOBILE_HTML)
        } else if (chain.request().url.host.contains("maps.wikimedia.org")) {
            builder.header("Referer", "https://maps.wikimedia.org/")
        }
        return chain.proceed(builder.build())
    }
}
