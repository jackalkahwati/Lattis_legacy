package cc.skylock.skylock.retofit;


import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cc.skylock.skylock.utils.SkylockConstant;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by prodapt on 06/08/15.
 */
public class RetofitRestAdapter {

    private static Retrofit retrofit = null;


    public static Retrofit getClient(String BASE_URL) {
        if (retrofit == null) {
            try {
                OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
                builder.addInterceptor(interceptor);
                builder.addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        //   String authValue = "Basic " + geberateBase64(SkylockConstant.userToken);
                        //         Request request = chain.request().newBuilder().addHeader("Authorization",  "Basic " + geberateBase64(SkylockConstant.userToken)).build();
                        Request request = chain.request().newBuilder().addHeader("Authorization", SkylockConstant.userToken)
                                .addHeader("Content-Type", "application/json").build();
                        return chain.proceed(request);
                    }
                });
//          OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new SessionRequestInterceptor()).build();
                OkHttpClient client = builder.build();

                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return retrofit;
    }

    public static String geberateBase64(String value) {
        // Sending side
        String base64Value = "";
        try {
            value += ":";
            byte[] data = value.getBytes("UTF-8");
            base64Value = Base64.encodeToString(data, Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return base64Value;
    }
}
