package com.cinebook.controller;

import com.cinebook.service.TmdbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller exposing real-time TMDB movie endpoints.
 * Never exposes the raw API key to the browser frontend.
 */
@RestController
@RequestMapping("/api/tmdb")
public class TmdbController {

    private final TmdbService tmdbService;

    public TmdbController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/now-playing")
    public ResponseEntity<Map<String, Object>> getNowPlaying(@RequestParam(value = "page", defaultValue = "1") int page) {
        return ResponseEntity.ok(tmdbService.getNowPlaying(page));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcoming(@RequestParam(value = "page", defaultValue = "1") int page) {
        return ResponseEntity.ok(tmdbService.getUpcoming(page));
    }

    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopular(@RequestParam(value = "page", defaultValue = "1") int page) {
        return ResponseEntity.ok(tmdbService.getPopular(page));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam("query") String query,
                                                      @RequestParam(value = "page", defaultValue = "1") int page) {
        return ResponseEntity.ok(tmdbService.searchMovies(query, page));
    }

    @GetMapping("/movie/{id}")
    public ResponseEntity<Map<String, Object>> getMovieDetails(@PathVariable("id") int tmdbId) {
        return ResponseEntity.ok(tmdbService.getMovieDetails(tmdbId));
    }
}
