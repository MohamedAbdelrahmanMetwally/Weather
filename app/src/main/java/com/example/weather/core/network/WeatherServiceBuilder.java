package com.example.weather.core.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherServiceBuilder {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static WeatherService service;

    public static WeatherService getInstance() {
        if (service == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            service = retrofit.create(WeatherService.class);
        }
        return service;
    }
}
