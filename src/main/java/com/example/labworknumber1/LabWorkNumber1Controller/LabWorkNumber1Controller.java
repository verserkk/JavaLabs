package com.example.labworknumber1.LabWorkNumber1Controller;

import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LabWorkNumber1Controller {

    @GetMapping("/book")
    public Map<String, String> getQueryBook(@RequestParam("name") String name) {
        return Collections.singletonMap("bookName", name);
    }

    @GetMapping("/book/{bookId}")
    public Map<String, Integer> getPathBook(@PathVariable("bookId") Integer bookId) {
        return Collections.singletonMap("bookId", bookId);
    }
}
