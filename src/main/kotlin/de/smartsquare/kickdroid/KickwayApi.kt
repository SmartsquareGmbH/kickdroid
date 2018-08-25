package de.smartsquare.kickdroid

import de.smartsquare.kickdroid.user.User
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author Ruben Gees
 */
interface KickwayApi {

    @POST("/authorization")
    fun authorize(@Body user: User): Completable
}
