package com.example.weather.core.util;
import java.util.List;
public class Model {
    public Main main;
    public List<Weather> weather;
    public Wind wind;
    public String name;
    public class Main {
        public float temp;
        public int humidity;
    }
    public class Weather {
        public String description;
        public String icon;
    }
    public class Wind {
        public float speed;
    }
}
