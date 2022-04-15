package com.omer.earthquakefinder.controller;

import com.omer.earthquakefinder.model.Earthquake;
import com.omer.earthquakefinder.service.EarthquakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.thymeleaf.util.ListUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/")
@SessionAttributes({"earthquakeList", "countOfDays"})
public class EarthquakeController {

    @ModelAttribute
    public void addAttributes(Model model) {
        if (!model.containsAttribute("earthquakeList")) {
            model.addAttribute("earthquakeList", null);
        }
    }

    @Autowired
    EarthquakeService earthquakeService;

    @GetMapping
    public String getHomePage(@RequestParam(value = "countOfDays", required = false) Integer count,
                              Model model) throws IOException {
        if (count != null) {
            List<Earthquake> earthquakeList = earthquakeService.getDayBeforeEarthquakeList(count);
            model.addAttribute("earthquakeList", earthquakeList);
            List<String> countryList = earthquakeList.stream()
                    .map(Earthquake::getCountry)
                    .collect(Collectors.toList());
            model.addAttribute("countryList", new HashSet<>(countryList));
        }
        return "/index";
    }

    @GetMapping("/search")
    public String searchEarthquakes(
            @RequestParam(value = "country") String country,
            @RequestParam(value = "countOfDays") Integer count,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @ModelAttribute("earthquakeList") List<Earthquake> earthquakeList,
            Model model) throws IOException {

        model.addAttribute("page", page);
        model.addAttribute("size", size);
        if (ListUtils.isEmpty(earthquakeList)) {
            earthquakeList = earthquakeService.getDayBeforeEarthquakeList(count);
            model.addAttribute("earthquakeList", earthquakeList);
        }
        List<Earthquake> earthquakes = earthquakeList.stream()
                .filter(earthquake -> earthquake.getCountry() != null && earthquake.getCountry().equals(country))
                .collect(Collectors.toList());

        Page<Earthquake> earthquakePage = earthquakeService.getEarthquakePage(PageRequest.of(page - 1, size), earthquakes);
        model.addAttribute("earthquakePage", earthquakePage);

        int totalPages = earthquakePage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> availablePages = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("availablePages", availablePages);
        } else if (country != null) {
            throw new RuntimeException("No Earthquakes were recorded past " + count + " days in " + country);
        }

        return "/fragments/search";
    }


    @ExceptionHandler(value = RuntimeException.class)
    public void handleRuntimeException(RuntimeException e, HttpServletResponse response) {
        response.setStatus(400);
        response.addHeader("error-message", e.getMessage());
    }
}
