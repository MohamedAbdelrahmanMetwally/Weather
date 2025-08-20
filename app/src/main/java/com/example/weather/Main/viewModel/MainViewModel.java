package com.example.weather.Main.viewModel;
import android.widget.Toast;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.weather.core.network.WeatherService;
import com.example.weather.core.network.WeatherServiceBuilder;
import com.example.weather.core.util.Model;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class MainViewModel extends ViewModel {
    private static final String API_KEY = "75a8b9afe06f1c8862c301bda6fbb03b";
    private final MutableLiveData<Model> weatherLiveData = new MutableLiveData<>();
    public LiveData<Model> getWeatherLiveData() {
        return weatherLiveData;
    }
    public void fetchCurrentWeather(Context context, Activity activity) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
            if (location != null) {
                callWeatherApi(location.getLatitude(), location.getLongitude(), context);
            } else {
                Toast.makeText(context, "Failed to get location", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void callWeatherApi(double lat, double lon, Context context) {
        WeatherService service = WeatherServiceBuilder.getInstance();
        Call<Model> call = service.getCurrentWeather(lat, lon, API_KEY, "metric", "en");
        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherLiveData.postValue(response.body());
                } else {
                    Log.e("API_RESPONSE", "Code: " + response.code());
                    Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                Toast.makeText(context, "Error in connection: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage(), t);
            }
        });
    }
}