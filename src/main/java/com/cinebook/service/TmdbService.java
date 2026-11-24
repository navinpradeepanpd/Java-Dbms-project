package com.cinebook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for fetching real-time cinematic movie data from TMDB (The Movie Database).
 * Features:
 * - Currently playing movies in India (region IN)
 * - Upcoming releases & Popular movies
 * - Real-time search with actual posters, backdrops, and overviews
 * - 15-minute in-memory caching to eliminate redundant API requests
 * - Graceful fallback when API key is unconfigured or rate-limited
 * - Official TMDB attribution
 */
@Service
public class TmdbService {

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private static final long CACHE_TTL_MS = 15 * 60 * 1000; // 15 Minutes

    @Value("${cinebook.tmdb.api-key:${TMDB_API_KEY:}}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private record CacheEntry(long timestamp, Map<String, Object> data) {}

    public Map<String, Object> getNowPlaying(int page) {
        String cacheKey = "now_playing_in_p" + page;
        return getOrFetch(cacheKey, () -> {
            String path = "/movie/now_playing?region=IN&page=" + page;
            return executeTmdbRequest(path, "Now Playing in India", getFallbackNowPlaying());
        });
    }

    public Map<String, Object> getUpcoming(int page) {
        String cacheKey = "upcoming_in_p" + page;
        return getOrFetch(cacheKey, () -> {
            String path = "/movie/upcoming?region=IN&page=" + page;
            return executeTmdbRequest(path, "Upcoming Screenings", getFallbackUpcoming());
        });
    }

    public Map<String, Object> getPopular(int page) {
        String cacheKey = "popular_in_p" + page;
        return getOrFetch(cacheKey, () -> {
            String path = "/movie/popular?region=IN&page=" + page;
            return executeTmdbRequest(path, "Popular Movies", getFallbackPopular());
        });
    }

    public Map<String, Object> searchMovies(String query, int page) {
        if (query == null || query.trim().isEmpty()) {
            return getPopular(1);
        }
        String cleanQuery = query.trim();
        String cacheKey = "search_" + cleanQuery.toLowerCase() + "_p" + page;

        return getOrFetch(cacheKey, () -> {
            try {
                String encoded = URLEncoder.encode(cleanQuery, StandardCharsets.UTF_8);
                String path = "/search/movie?query=" + encoded + "&page=" + page;
                return executeTmdbRequest(path, "Search: " + cleanQuery, filterFallbackByQuery(cleanQuery));
            } catch (Exception e) {
                return filterFallbackByQuery(cleanQuery);
            }
        });
    }

    public Map<String, Object> getMovieDetails(int tmdbId) {
        String cacheKey = "movie_details_" + tmdbId;
        return getOrFetch(cacheKey, () -> {
            String path = "/movie/" + tmdbId + "?append_to_response=videos,credits";
            return executeTmdbRequest(path, "Movie Details #" + tmdbId, getFallbackDetails(tmdbId));
        });
    }

    private Map<String, Object> getOrFetch(String cacheKey, java.util.function.Supplier<Map<String, Object>> supplier) {
        CacheEntry entry = cache.get(cacheKey);
        long now = System.currentTimeMillis();
        if (entry != null && (now - entry.timestamp() < CACHE_TTL_MS)) {
            return entry.data();
        }

        Map<String, Object> fresh = supplier.get();
        cache.put(cacheKey, new CacheEntry(now, fresh));
        return fresh;
    }

    private Map<String, Object> executeTmdbRequest(String pathAndQuery, String label, Map<String, Object> fallback) {
        String trimmedKey = apiKey != null ? apiKey.trim() : "";
        if (trimmedKey.isEmpty()) {
            Map<String, Object> result = new HashMap<>(fallback);
            result.put("source", "curated_catalog");
            result.put("notice", "Live TMDB API key not configured. Set TMDB_API_KEY environment variable to enable live API.");
            attachAttribution(result);
            return result;
        }

        try {
            boolean isBearer = trimmedKey.startsWith("ey") || trimmedKey.length() > 50;
            String urlStr;
            if (isBearer) {
                urlStr = TMDB_BASE_URL + pathAndQuery;
            } else {
                char separator = pathAndQuery.contains("?") ? '&' : '?';
                urlStr = TMDB_BASE_URL + pathAndQuery + separator + "api_key=" + trimmedKey;
            }

            URI uri = URI.create(urlStr);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Accept", "application/json");

            if (isBearer) {
                conn.setRequestProperty("Authorization", "Bearer " + trimmedKey);
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    JsonNode node = objectMapper.readTree(reader);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mapped = objectMapper.convertValue(node, Map.class);
                    mapped.put("source", "live_tmdb");
                    attachAttribution(mapped);
                    return mapped;
                }
            } else {
                System.err.println("TMDB API responded with HTTP " + code + " for " + label + ". Using fallback.");
            }
        } catch (Exception e) {
            System.err.println("Notice connecting to TMDB API: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>(fallback);
        result.put("source", "curated_fallback");
        attachAttribution(result);
        return result;
    }

    private void attachAttribution(Map<String, Object> map) {
        map.put("attribution", "This product uses the TMDB API but is not endorsed or certified by TMDB.");
        map.put("attribution_logo", "https://www.themoviedb.org/assets/2/v4/logos/v2/blue_square_2-d537fb228cf3ded904ef09b136fe3fec72548ebc1fea3fbbd1ad9e36364db38b.svg");
    }

    // =========================================================================
    // HIGH-QUALITY CURATED REAL THEATRICAL METADATA FALLBACK
    // =========================================================================

    private Map<String, Object> getFallbackNowPlaying() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(createMovieMap(157336, "Interstellar", 169, "2014-11-05", 8.7,
                "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", "/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
                "When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot is tasked to pilot a spacecraft to find a new planet for humans.",
                List.of("Adventure", "Drama", "Science Fiction"), "zSWdZVtXT7E"));

        list.add(createMovieMap(27205, "Inception", 148, "2010-07-15", 8.8,
                "/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg", "/s3TBrRGB1iav7gFOCNx3H31MoES.jpg",
                "Cobb, a skilled thief who commits corporate espionage by infiltrating the subconscious of his targets, is offered a chance to regain his old life.",
                List.of("Action", "Science Fiction", "Adventure"), "YoHD9XEInc0"));

        list.add(createMovieMap(155, "The Dark Knight", 152, "2008-07-16", 9.0,
                "/qJ2tW6WMUDux911r6m7haRef0WH.jpg", "/nMKdUUepR0i5zn0y1T4CsSB5chy.jpg",
                "Batman raises the stakes in his war on crime with the help of Lt. Jim Gordon and District Attorney Harvey Dent against the psychopathic Joker.",
                List.of("Drama", "Action", "Crime", "Thriller"), "EXeTwQWrcwY"));

        list.add(createMovieMap(872585, "Oppenheimer", 180, "2023-07-19", 8.9,
                "/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg", "/s3TBrRGB1iav7gFOCNx3H31MoES.jpg",
                "The story of J. Robert Oppenheimer's role in the development of the atomic bomb during World War II.",
                List.of("Drama", "History"), "uYPbbksJxIg"));

        list.add(createMovieMap(558449, "Gladiator II", 148, "2024-11-13", 8.1,
                "/2cxhvwyEwRlysAmRH4iodkvo0z5.jpg", "/nMKdUUepR0i5zn0y1T4CsSB5chy.jpg",
                "Years after witnessing the death of the revered hero Maximus, Lucius must enter the Colosseum after his home is conquered by tyrannical Emperors.",
                List.of("Action", "Adventure", "Drama"), "4rgYUipGJNo"));

        list.add(createMovieMap(76600, "Avatar: The Way of Water", 192, "2022-12-14", 7.6,
                "/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg", "/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
                "Set more than a decade after the events of the first film, Jake Sully and Neytiri must explore the regions of Pandora.",
                List.of("Science Fiction", "Adventure", "Action"), "d9MyW72ELq0"));

        Map<String, Object> res = new HashMap<>();
        res.put("page", 1);
        res.put("results", list);
        res.put("total_pages", 1);
        res.put("total_results", list.size());
        return res;
    }

    private Map<String, Object> getFallbackUpcoming() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(createMovieMap(569094, "Spider-Man: Beyond the Spider-Verse", 140, "2025-06-25", 8.9,
                "/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg", "/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
                "Miles Morales embarks across the Multiverse alongside Gwen Stacy and a new team of Spider-People.",
                List.of("Animation", "Action", "Adventure"), "shW9i6k8cB0"));

        list.add(createMovieMap(933260, "The Batman: Part II", 165, "2026-10-02", 8.6,
                "/qJ2tW6WMUDux911r6m7haRef0WH.jpg", "/nMKdUUepR0i5zn0y1T4CsSB5chy.jpg",
                "The continuation of the Batman saga directed by Matt Reeves.",
                List.of("Action", "Crime", "Drama"), "mqqft2x_Aa4"));

        Map<String, Object> res = new HashMap<>();
        res.put("page", 1);
        res.put("results", list);
        res.put("total_pages", 1);
        res.put("total_results", list.size());
        return res;
    }

    private Map<String, Object> getFallbackPopular() {
        return getFallbackNowPlaying();
    }

    private Map<String, Object> filterFallbackByQuery(String query) {
        String lower = query.toLowerCase();
        List<Map<String, Object>> all = new ArrayList<>();
        all.addAll((List<Map<String, Object>>) getFallbackNowPlaying().get("results"));
        all.addAll((List<Map<String, Object>>) getFallbackUpcoming().get("results"));

        List<Map<String, Object>> filtered = all.stream()
                .filter(m -> ((String) m.get("title")).toLowerCase().contains(lower) ||
                             ((String) m.get("overview")).toLowerCase().contains(lower))
                .distinct()
                .toList();

        Map<String, Object> res = new HashMap<>();
        res.put("page", 1);
        res.put("results", filtered.isEmpty() ? all : filtered);
        res.put("total_pages", 1);
        res.put("total_results", filtered.size());
        return res;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getFallbackDetails(int tmdbId) {
        List<Map<String, Object>> results = (List<Map<String, Object>>) getFallbackNowPlaying().get("results");
        for (Map<String, Object> m : results) {
            if (m.get("id").equals(tmdbId)) return m;
        }
        return results.isEmpty() ? Collections.emptyMap() : results.get(0);
    }

    private Map<String, Object> createMovieMap(int id, String title, int runtime, String releaseDate, double rating,
                                               String posterPath, String backdropPath, String overview,
                                               List<String> genres, String trailerKey) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("title", title);
        m.put("runtime", runtime);
        m.put("release_date", releaseDate);
        m.put("vote_average", rating);
        m.put("poster_path", posterPath);
        m.put("backdrop_path", backdropPath);
        m.put("poster_url", "https://image.tmdb.org/t/p/w500" + posterPath);
        m.put("backdrop_url", "https://image.tmdb.org/t/p/w1280" + backdropPath);
        m.put("overview", overview);
        m.put("genres", genres);
        m.put("trailer_key", trailerKey);
        m.put("trailer_url", "https://www.youtube.com/watch?v=" + trailerKey);
        return m;
    }
}
