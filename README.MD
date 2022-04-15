# **EARTHQUAKE FINDER**

This is a single page web application for finding earthquakes at last given days a in specific coutnry

| Technology       |
|------------------|
| Spring Boot      |
| JQuery           |
| Thymeleaf Engine |
| HTML, CSS        |

## How to run application

```shell
 git clone https://github.com/omeratila7/earthquake-finder.git
 cd earthquake-finder
 mvn spring-boot:run
```

- Then go to **http://localhost:8080** with your browser.

## Project Structure

```
main
├── java
│   └── com
│       └── omer
│           └── earthquakefinder
│               ├── controller
│               │   └── EarthquakeController.java
│               ├── EarthquakeFinderApplication.java
│               ├── model
│               │   └── Earthquake.java
│               └── service
│                   ├── EarthquakeService.java
│                   └── JsonReader.java
└── resources
    ├── application.properties
    ├── static
    │   ├── css
    │   │   ├── app.css
    │   │   └── bootstrap.css
    │   └── jsapp
    │       └── script.js
    └── templates
        ├── fragments
        │   ├── modal.html
        │   └── search.html
        └── index.html


```

USGS Earthquake api provides sources code, but it is code of network contributors.So the api doesn't provide country
information. Api provides place information which created with GEONames database to find most close city in
300km[* ](https://earthquake.usgs.gov/data/comcat/index.php#place). This application uses that place information to find
which country that place belongs to with another api of USGS to find geolocation and saves data
into [knownLocations.txt](./location/knownLocations.txt).

## How to use application

![search](./images/search.gif)