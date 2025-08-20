package com.example.weather.Main.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.weather.Main.util.factoryViewModel;
import com.example.weather.Main.viewModel.MainViewModel;
import com.example.weather.R;
import com.example.weather.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private ActivityMainBinding binding;
    private factoryViewModel factory;
    private MainViewModel mainViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        factory = new factoryViewModel();
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkLocationPermission();
        mainViewModel.getWeatherLiveData().observe(this, model -> {
            binding.progressBar.setVisibility(View.GONE);
            if (model != null) {
                binding.cityText.setText(model.name);
                binding.tempText.setText(String.valueOf(model.main.temp) + "°C");
                binding.descText.setText(model.weather.get(0).description);
                binding.humidityText.setText("Humidity: " + model.main.humidity + "%");
                binding.windText.setText("Wind Speed: " + model.wind.speed + " m/s");
                Glide.with(this)
                        .load("https://openweathermap.org/img/wn/" + model.weather.get(0).icon + "@2x.png")
                        .into(binding.weatherIcon);
            } else {
                Toast.makeText(this, "Failure to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getUserLocation();
        }
    }
    private void getUserLocation() {
        binding.progressBar.setVisibility(View.VISIBLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    mainViewModel.fetchCurrentWeather(this,this);
                } else {
                    requestNewLocation();
                }
            });
        }
    }
    private void requestNewLocation() {
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdates(1)
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location freshLocation = locationResult.getLastLocation();
                    if (freshLocation != null) {
                        mainViewModel.fetchCurrentWeather(MainActivity.this,MainActivity.this);
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
                    }
                }
            }, getMainLooper());
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "You should grant location permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
}