package data.core;

import java.io.IOException;
import data.network.Kazisafe;
import data.network.OnTokenRefreshedListener;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Interceptor.Chain;
import okhttp3.Request.Builder;
import retrofit2.Call;
import data.helpers.Token;

public class HttpInterceptor implements Interceptor {

    String curentToken;
    OnTokenRefreshedListener onTokenRefreshedListener;

    public HttpInterceptor(String curentToken) {
        this.curentToken = curentToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request rq = chain.request();
        Builder builder = rq.newBuilder();
       
        this.setHeader(builder, this.curentToken);
        rq = builder.build();
        Response resp = chain.proceed(rq);
        if (resp.code() == 403 || resp.code() == 401) {
            Kazisafe apiService = KazisafeServiceFactory.getInstanceRefresh().create(Kazisafe.class);
            Token token = new Token();
            token.setToken(this.curentToken);
            Call call = apiService.refreshToken(token);
            try {
                retrofit2.Response responseCall = call.execute();
                ResponseBody responseRequest = (ResponseBody) responseCall.body();
                if (responseRequest != null) {
                    this.curentToken = responseRequest.string();
                    token.setToken(this.curentToken);
                    this.setHeader(builder, this.curentToken);
                    rq = builder.build();
                    resp.close();
                    resp = chain.proceed(rq);
                    this.notifyTokenRefresh(token);
                }
            } catch (Exception var10) {
                var10.printStackTrace();
            }
        }

        return resp;
    }

    private void notifyTokenRefresh(Token tkn) {
        if (this.onTokenRefreshedListener != null) {
            this.onTokenRefreshedListener.onTokenRefreshed(tkn);
        }

    }

    public void setOnTokenRefreshedListener(OnTokenRefreshedListener onTokenRefreshedListener) {
        this.onTokenRefreshedListener = onTokenRefreshedListener;
    }

    public void setHeader(Builder builder, String token) {
        if (token != null) {
            builder.header("Authorization", String.format("Bearer %s", token));
        }

    }
}
