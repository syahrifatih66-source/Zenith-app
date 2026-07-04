package com.example.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class YahooResponse(
    val chart: ChartData? = null
)

@JsonClass(generateAdapter = true)
data class ChartData(
    val result: List<ChartResult>? = null,
    val error: YahooError? = null
)

@JsonClass(generateAdapter = true)
data class ChartResult(
    val meta: MetaData? = null
)

@JsonClass(generateAdapter = true)
data class MetaData(
    val currency: String? = null,
    val symbol: String? = null,
    val regularMarketPrice: Double? = null,
    val chartPreviousClose: Double? = null
)

@JsonClass(generateAdapter = true)
data class YahooError(
    val code: String? = null,
    val description: String? = null
)

interface YahooFinanceApi {
    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String
    ): YahooResponse
}

object YahooFinanceClient {
    private const val BASE_URL = "https://query1.finance.yahoo.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val userAgentInterceptor = Interceptor { chain ->
        val originalRequest: Request = chain.request()
        val requestWithUserAgent: Request = originalRequest.newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .header("Accept", "application/json")
            .build()
        chain.proceed(requestWithUserAgent)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val api: YahooFinanceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(YahooFinanceApi::class.java)
    }

    /**
     * Safely queries current stock price.
     * Appends '.JK' if the symbol has no dot (usually Indonesia stock tickers).
     */
    suspend fun fetchStockPrice(ticker: String): Double? {
        val formatedTicker = if (ticker.contains(".")) ticker.trim().uppercase() else "${ticker.trim().uppercase()}.JK"
        return try {
            val response = api.getChart(formatedTicker)
            val price = response.chart?.result?.firstOrNull()?.meta?.regularMarketPrice
            price
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
