/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/history")
public class HistoryController {

    private final DataHistoryRepository dataHistoryRepository;

    @GetMapping("/{type}")
    public List<DataHistory> findHistoryByType(@AuthenticationPrincipal UserDetails user, @PathVariable DataHistory.Type type) {
        return dataHistoryRepository.findByUsernameAndType(user.getUsername(), type.name());
    }
}
