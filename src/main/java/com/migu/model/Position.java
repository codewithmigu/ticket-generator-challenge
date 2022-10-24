package com.migu.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Position {
    private final Integer line;
    private final Integer column;

    public static Position of(int line, int column) {
        return Position.builder()
                .line(line)
                .column(column)
                .build();
    }
}
