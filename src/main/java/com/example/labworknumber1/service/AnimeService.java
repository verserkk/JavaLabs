package com.example.labworknumber1.service;

import com.example.labworknumber1.model.AnimeSeries;
import org.springframework.stereotype.Service;

@Service
public class AnimeService {
    public AnimeSeries createAnimeSeries(String name, Integer id) {
        return new AnimeSeries(name, id);
    }
}
