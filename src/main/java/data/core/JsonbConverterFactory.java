/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data.core;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import jakarta.json.bind.config.PropertyVisibilityStrategy;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 *
 * @author endeleya
 */
public class JsonbConverterFactory extends Converter.Factory {
 private final Jsonb jsonb;

    public static JsonbConverterFactory create() {
       JsonbConfig config = new JsonbConfig()
            .withPropertyVisibilityStrategy(new PropertyVisibilityStrategy() {
                @Override
                public boolean isVisible(Field field) {
                    return true;
                }

                @Override
                public boolean isVisible(Method method) {
                    return true;
                }
            })
            .setProperty("org.eclipse.yasson.YassonConfig.ZERO_TIME_PARSE_DEFAULTING", true);

        Jsonb jsonb = JsonbBuilder.create(config);
        return new JsonbConverterFactory(jsonb);
    }

    private JsonbConverterFactory(Jsonb jsonb) {
        if (jsonb == null) throw new NullPointerException("jsonb == null");
        this.jsonb = jsonb;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new JsonbResponseBodyConverter<>(jsonb, type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
                                                          Annotation[] methodAnnotations, Retrofit retrofit) {
        return new JsonbRequestBodyConverter<>(jsonb, type);
    }

    final class JsonbResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private final Jsonb jsonb;
        private final Type type;

        JsonbResponseBodyConverter(Jsonb jsonb, Type type) {
            this.jsonb = jsonb;
            this.type = type;
        }

        @Override
        public T convert(ResponseBody value) throws IOException {
            try {
                return jsonb.fromJson(value.charStream(), type);
            } finally {
                value.close();
            }
        }
    }

    final class JsonbRequestBodyConverter<T> implements Converter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");

        private final Jsonb jsonb;
        private final Type type;

        JsonbRequestBodyConverter(Jsonb jsonb, Type type) {
            this.jsonb = jsonb;
            this.type = type;
        }

        @Override
        public RequestBody convert(T value) throws IOException {
            try {
                String json = jsonb.toJson(value, type);
                return RequestBody.create(MEDIA_TYPE, json);
            } catch (JsonbException e) {
                throw new IOException(e);
            }
        }
    }
 
    
}
