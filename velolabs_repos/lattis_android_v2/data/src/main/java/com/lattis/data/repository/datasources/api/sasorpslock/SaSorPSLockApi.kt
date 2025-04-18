package com.lattis.data.repository.datasources.api.sasorpslock

import com.lattis.domain.models.sasorpslock.SaSOrPSLockUnlockTokenResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SaSorPSLockApi {
//    @Headers(
//        "Authorization: eyJraWQiOiJJNXhKZUdaaEpPWDQ5bFYyYTZtUjBRRER0UDFTWFpvRGp1NFZNc3FVd3JVPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI3ajNtYmFoOGJ1a21qOTI2azc5amd0NTdzaSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoicHVibGljLWFwaVwvcHVibGljLWFwaSIsImF1dGhfdGltZSI6MTY0NzIzOTI4NywiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LWNlbnRyYWwtMS5hbWF6b25hd3MuY29tXC9ldS1jZW50cmFsLTFfZ2xYbWRsWGpPIiwiZXhwIjoxNjQ3MjQyODg3LCJpYXQiOjE2NDcyMzkyODcsInZlcnNpb24iOjIsImp0aSI6Ijc2NjEwYjMxLTgzZDUtNGI0ZC1hYzExLTdkODg0ZjExZTI0YyIsImNsaWVudF9pZCI6IjdqM21iYWg4YnVrbWo5MjZrNzlqZ3Q1N3NpIn0.IJNZ9EXSEkvi_NoBbC82pkRsv83JL61kn32QUfmLtGHiIvS0Bs7Q-I6Sxw7PGqNwIMQ-y5HUiFhQA7C27WZ7hjJshRE8M19QQID25N8lh_mcHIffndJuz3zg_uR8q_3HeSpUk-Ha7nVnRu3cLlq0E1eVqV1PUhW6HziMHOIEHeXM-Rd8OupIP3arKHbaaWt9WNjAxDFUHSL8qNAug_KOdMGdlAiYyG7nHHInQEdmEnOsqKADhynA9kq2xUARyEOS4kDtrMzAbwrFnIVZCHpobws0a7aIJXZX8kb3EMTLguoEYmskaMXOU7Ug-uhW3hB6GlSmX-6t0duz4D7l5IIw8Q"
//    )
//
//    @GET("users/e36bba02-3c34-40d2-8f54-b133c889fcf7/accessrights/42/device/{device_id}/token/{nonce}")
//    fun getUnlockToken(@Path("device_id") device_id:String,@Path("nonce") nonce:String): Observable<SaSOrPSLockUnlockToken>

    @GET("api/sas/credentials/{deviceId}/{tokenId}")
    fun getUnlockToken(@Path("deviceId") deviceId:String,@Path("tokenId") tokenId:String,@Query("fleetId") fleetId:Int): Observable<SaSOrPSLockUnlockTokenResponse>
}