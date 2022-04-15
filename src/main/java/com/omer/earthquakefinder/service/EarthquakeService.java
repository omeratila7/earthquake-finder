package com.omer.earthquakefinder.service;

import com.omer.earthquakefinder.model.Earthquake;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:application.properties")
public class EarthquakeService {
    Map<String, String> knownLocations;
    DateFormat dateFormat;
    @Autowired
    Environment environment;
    @Autowired
    JsonReader jsonReader;

    private static final Logger LOG = LoggerFactory.getLogger(EarthquakeService.class);

    public EarthquakeService() {
        knownLocations = new HashMap<>();
        loadLocationData();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public List<Earthquake> getDayBeforeEarthquakeList(int count) {
        Date now = new Date();
        Date countDaysAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(count));
        String endTime = dateFormat.format(now);
        String startTime = dateFormat.format(countDaysAgo);
        try {
            List<Earthquake> features = getEarthquakeListFromApi(startTime, endTime);
            return features;
        } catch (IOException e) {
            LOG.error("IO Exception", e);
            throw new RuntimeException("Cannot get data from api");
        }
    }

    public Page<Earthquake> getEarthquakePage(Pageable pageable, List<Earthquake> earthquakes) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startIndex = currentPage * pageSize;
        List<Earthquake> list;
        if (earthquakes.size() < startIndex) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startIndex + pageSize, earthquakes.size());
            list = earthquakes.subList(startIndex, toIndex);
        }
        Page<Earthquake> page = new PageImpl(list, PageRequest.of(currentPage, pageSize), earthquakes.size());
        return page;
    }

    private List<Earthquake> getEarthquakeListFromApi(String startTime, String endTime) throws IOException {
        URL url = new URL("https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=" + startTime + "&endtime=" + endTime);
        JSONObject jsonObject = jsonReader.readJsonFromUrl(url);
        JSONArray features = (JSONArray) jsonObject.get("features");

        List<Earthquake> earthquakeList = new ArrayList<>();
        for (int i = 0; i < features.length(); i++) {
            JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
            JSONArray coordinates = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
            Earthquake earthquake = new Earthquake();
            if (!properties.isNull("mag")) {
                earthquake.setMag(properties.getDouble("mag"));
            }
            String place = null;
            if (!properties.isNull("place")) {
                place = properties.getString("place");
                earthquake.setPlace(place);
            }
            if (!properties.isNull("time")) {
                earthquake.setTime(new Date(properties.getLong("time")));
            }
            if (coordinates != null) {
                Double latitude = coordinates.getDouble(1);
                Double longitude = coordinates.getDouble(0);
                if (place != null) {
                    String placeLocation = place.split(",")[place.split(",").length - 1].trim();
                    String country;
                    if (knownLocations.containsKey(placeLocation)) {
                        country = knownLocations.get(placeLocation);
                    } else {
                        country = getCountryFromLocation(latitude, longitude);
                        appendLocationData(placeLocation, country);
                    }
                    earthquake.setCountry(country);
                }
            }
            earthquakeList.add(earthquake);
        }
        return earthquakeList;
    }

    private String getCountryFromLocation(Double latitude, Double longitude) throws IOException {
        URL url = new URL("https://earthquake.usgs.gov/ws/geoserve/places.json?latitude=" + latitude + "&longitude=" + longitude + "&type=geonames&maxradiuskm=300&limit=1");

        try {
            String country = jsonReader.readJsonFromUrl(url)
                    .getJSONObject("geonames")
                    .getJSONArray("features")
                    .getJSONObject(0)
                    .getJSONObject("properties")
                    .getString("country_name");
            return country;
        } catch (Exception e) {
            return "Ocean";
        }
    }

    private void appendLocationData(String placeLocation, String country) {
        knownLocations.put(placeLocation, country);
        File file = new File("location/knownLocations.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(placeLocation + ":" + country + "\n");
            writer.close();
        } catch (IOException e) {
            LOG.error("IO Exception", e);
            throw new RuntimeException("Cannot write data to file");
        }
    }

    private void loadLocationData() {
        File file = new File("location/knownLocations.txt");
        if (!file.exists()) {
            return;
        }
        try {
            Scanner reader = new Scanner(file);
            while (reader.hasNext()) {
                String line = reader.nextLine();
                if (isBlank(line)) {
                    continue;
                }
                String[] location = line.split(":");
                knownLocations.put(location[0], location[1]);
            }
            reader.close();
        } catch (IOException e) {
            LOG.error("IO Exception", e);
            throw new RuntimeException("Cannot load data from file");
        }

    }

    private static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
}
