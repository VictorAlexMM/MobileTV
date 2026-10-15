package com.iptvplayer.app.parser.xtream

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamApiService {

    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") user: String,
        @Query("password") pass: String
    ): XtreamAuthResponse

    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String? = null
    ): List<XtreamLiveStream>

    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_streams",
        @Query("category_id") categoryId: String? = null
    ): List<XtreamVodStream>

    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_series_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_series",
        @Query("category_id") categoryId: String? = null
    ): List<XtreamSeriesItem>

    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: String
    ): ResponseBody
}
