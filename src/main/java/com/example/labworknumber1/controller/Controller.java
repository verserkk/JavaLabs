package com.example.labworknumber1.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Controller {

    @GetMapping("/anime")
    public Map<String, String> getQueryBook(@RequestParam("name") String name) {
        return Collections.singletonMap("animeName", name);
    }

    @GetMapping("/anime/{animeId}")
    public Map<String, Integer> getPathBook(@PathVariable("animeId") Integer bookId) {
        return Collections.singletonMap("animeId", bookId);
    }
}
