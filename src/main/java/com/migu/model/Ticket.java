package com.migu.model;


import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
@Getter
@Builder
public class Ticket {
    private final Map<Position, Integer> positionValueMap;

    public static Ticket of(Map<Position, Integer> positionValueMap) {
        return Ticket.builder()
                .positionValueMap(positionValueMap)
                .build();
    }
}
