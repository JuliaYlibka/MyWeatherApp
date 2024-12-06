package com.example.myweatherapp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.loader.content.AsyncTaskLoader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private AutoCompleteTextView autoCompleteTextView;
    private TextView tV_result, Tv_Temp, TV_FeelingWeather, Tv_SpeedWind, Tv_GoWind, Tv_StartSun, Tv_endSun, Tv_humid;
    private String city;
    String[] cities = {
            // Россия
            "Москва", "Санкт-Петербург", "Новосибирск",
            // США
            "Нью-Йорк", "Лос-Анджелес", "Чикаго",
            // Великобритания
            "Лондон", "Бирмингем", "Манчестер",
            // Германия
            "Берлин", "Гамбург", "Мюнхен",
            // Франция
            "Париж", "Марсель", "Лион",
            // Испания
            "Мадрид", "Барселона", "Валенсия",
            // Италия
            "Рим", "Милан", "Неаполь",
            // Канада
            "Торонто", "Монреаль", "Ванкувер",
            // Австралия
            "Сидней", "Мельбурн", "Брисбен",
            // Индия
            "Мумбаи", "Дели", "Бангалор"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tV_result = findViewById(R.id.TV_Status);
        Tv_Temp = findViewById(R.id.Tv_Temp);
        Tv_humid = findViewById(R.id.Tv_Humid);
        Tv_GoWind = findViewById(R.id.Tv_goWind);
        TV_FeelingWeather = findViewById(R.id.Tv_Feeling_weather);
        Tv_SpeedWind = findViewById(R.id.Tv_speedWind);
        Tv_StartSun = findViewById(R.id.Tv_StartSun);
        Tv_endSun = findViewById(R.id.Tv_EndSun);
        autoCompleteTextView = findViewById(R.id.autoCompleteCity);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
        autoCompleteTextView.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Проверяем разрешения
        Log.d("MainActivity", "Запрос разрешения на доступ к местоположению");
            getLastLocation();
        }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем разрешение
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
        fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                city = getCityName(latitude, longitude);
                                autoCompleteTextView.setText(city); // Устанавливаем название города в AutoCompleteTextView
                            } else {
                                Toast.makeText(MainActivity.this, "Не удалось получить местоположение", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private String getCityName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getLocality(); // Возвращаем название города
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Город не найден";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation(); // Получаем местоположение, если разрешение предоставлено
            } else {
                Toast.makeText(this, "Разрешение на доступ к местоположению отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void GetClimate(View view) {
        city = String.valueOf(autoCompleteTextView.getText());

        final String key = "e14d2040f06f9edac30b450b0e5fc611";
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city.trim() + "&appid=" + key+"&lang=ru&units=metric";
        new GetUrlData().execute(url);
    }

    private class GetUrlData extends AsyncTask<String,String,String> {


        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result){
            if (result == null) {
                tV_result.setText("Ошибка при получении данных. Попробуйте снова.");
                return; // Выходите из метода, если результат null
            }
            try {
                JSONObject obj = new JSONObject(result);
                JSONArray weatherArray = obj.getJSONArray("weather");
                JSONObject weatherObject = weatherArray.getJSONObject(0); // первый элемент массива
                tV_result.setText(weatherObject.getString("description")); // Получаем описание
                Tv_Temp.setText(String.valueOf( obj.getJSONObject("main").getDouble("temp")));
                TV_FeelingWeather.setText(String.valueOf( obj.getJSONObject("main").getDouble("feels_like")));
                Tv_SpeedWind.setText(String.valueOf( obj.getJSONObject("wind").getDouble("speed")));
                Tv_GoWind.setText( getGoWind(obj.getJSONObject("wind").getDouble("speed")));
                Long Sunrise = obj.getJSONObject("sys").getLong("sunrise");
                Instant instant = Instant.ofEpochSecond(Sunrise);
                ZoneId zoneId = ZoneId.systemDefault();
                LocalDateTime dateTime = LocalDateTime.ofInstant(instant, zoneId);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDate = dateTime.format(formatter);

                Tv_StartSun.setText(formattedDate);
                Long Sunset = obj.getJSONObject("sys").getLong("sunset");

                instant = Instant.ofEpochSecond(Sunset);
                dateTime = LocalDateTime.ofInstant(instant, zoneId);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                formattedDate = dateTime.format(formatter);

                Tv_endSun.setText(String.valueOf(formattedDate));
                Tv_humid.setText(String.valueOf( obj.getJSONObject("main").getInt("humidity")));

            } catch (JSONException e) {
                tV_result.setText("Ошибка при обработке данных о погоде. Пожалуйста, попробуйте снова.");
            }
        }
        private String getGoWind(double deg) {
            if (deg < 0 || deg >= 360) return "Некорректный ввод";

            if (deg == 0 || deg == 360) return "С";
            if (deg == 90) return "В";
            if (deg == 180) return "Ю";
            if (deg == 270) return "З";

            if (deg < 90) return "С-В";
            if (deg < 180) return "Ю-В";
            if (deg < 270) return "Ю-З";
            return "С-З";
        }

    }
}
