package com.example.weather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "75a8b9afe06f1c8862c301bda6fbb03b"; // 🔐 ضع مفتاح OpenWeatherMap هنا
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    private ImageView weatherIcon;
    private TextView cityText, tempText, descText, humidityText, windText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        weatherIcon = findViewById(R.id.weatherIcon);
        cityText = findViewById(R.id.cityText);
        tempText = findViewById(R.id.tempText);
        descText = findViewById(R.id.descText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        getUserLocation();
    }
    private void getUserLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                fetchWeather(location);
            } else {
                Toast.makeText(this, "تعذر الحصول على الموقع", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchWeather(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherService service = retrofit.create(WeatherService.class);
        Call<Model> call = service.getCurrentWeather(lat, lon, API_KEY, "metric", "en");
        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Model model = response.body();
                    String iconCode = model.weather.get(0).icon;
                    String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
                    Glide.with(MainActivity.this).load(iconUrl).into(weatherIcon);
                    cityText.setText("city: " + model.name);
                    tempText.setText("temprature: " + model.main.temp + "°C");
                    descText.setText("description: \n" + model.weather.get(0).description);
                    humidityText.setText("humidity:   " + model.main.humidity + "%");
                    windText.setText("wind speed :  " + model.wind.speed + " m/s");
                } else {
                    Log.e("API_RESPONSE", "Code: " + response.code());
                    Toast.makeText(MainActivity.this, "فشل في تحميل البيانات", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                Toast.makeText(MainActivity.this, "خطأ في الاتصال: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage(), t);
            }
        });
    }
}