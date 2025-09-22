package com.chart;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CurrencyFetcher {

    /**
     * Fetches currency trend for the past 'days' days using Frankfurter API (no API
     * key required).
     * 
     * @param base   Base currency, e.g., "USD"
     * @param target Target currency, e.g., "INR"
     * @param days   Number of past days to fetch
     * @return List of exchange rates in chronological order
     */
    public static List<Double> fetchCurrencyTrend(String base, String target, int days) {
        List<Double> rates = new ArrayList<>();
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);

            // Frankfurter API URL for timeseries
            String urlStr = String.format(
                    "https://api.frankfurter.app/%s..%s?from=%s&to=%s",
                    startDate, endDate, base, target);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new URL(urlStr));
            JsonNode ratesNode = root.path("rates");

            // Frankfurter returns a JSON object with dates as fields
            ratesNode.fieldNames().forEachRemaining(date -> {
                double rate = ratesNode.get(date).get(target).asDouble();
                rates.add(rate);
            });

        } catch (Exception e) {
            System.out.println("Failed to fetch currency data: " + e.getMessage());
        }
        return rates;
    }
}
