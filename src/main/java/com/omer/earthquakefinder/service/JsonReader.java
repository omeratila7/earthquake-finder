package com.omer.earthquakefinder.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

@Service
public class JsonReader {
    static final Logger LOG = LoggerFactory.getLogger(JsonReader.class);

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public JSONObject readJsonFromUrl(URL url) {
        InputStream is;
        try {
            is = url.openStream();
        } catch (IOException e) {
            LOG.error("IO Exception", e);
            throw new RuntimeException("Cannot get data from api try with small amount of count");
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            rd.close();
            return json;
        } catch (IOException e) {
            LOG.error("IO Exception", e);
            throw new RuntimeException("Cannot read data from buffer");
        }
    }
}
