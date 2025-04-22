package com.hambugi.batchServer.controller;

import com.hambugi.batchServer.service.CulturalEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CulturalEventController {
    private final CulturalEventService culturalEventService;

    public CulturalEventController(CulturalEventService culturalEventService) {
        this.culturalEventService = culturalEventService;
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        culturalEventService.fetchAllEventDataParallel();
        return ResponseEntity.ok().build();
    }
}
