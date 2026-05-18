package org.tensorflow.lite.examples.imageclassification;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @FormUrlEncoded
    @POST("/register")
    Call<String> registerUser(
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("/profile/{uid}")
    Call<String> getProfile(
            @Path("uid") String uid
    );

    @FormUrlEncoded
    @PUT("/update-profile")
    Call<String> updateProfile(
            @Field("uid") String uid,
            @Field("name") String name,
            @Field("imageUrl") String imageUrl
    );
}