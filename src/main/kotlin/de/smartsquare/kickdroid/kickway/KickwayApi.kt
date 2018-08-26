package de.smartsquare.kickdroid.kickway

import de.smartsquare.kickdroid.statistics.Player
import de.smartsquare.kickdroid.user.User
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * @author Ruben Gees
 */
interface KickwayApi {

    @POST("/authorization")
    fun authorize(@Body user: User): Completable

    @GET("/statistics/topten/soloq")
    fun soloQStatistics(): Single<List<Player>>

    @GET("/statistics/topten/duoq")
    fun duoQStatistics(): Single<List<Player>>

    @GET("/statistics/topten/flexq")
    fun flexQStatistics(): Single<List<Player>>
}
