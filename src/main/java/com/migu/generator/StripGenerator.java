package com.migu.generator;

import com.migu.model.Strip;
import lombok.Data;

@Data
public class StripGenerator implements Generator<Strip> {

    private final TicketGenerator ticketGenerator;

    @Override
    public Strip generate() {
        return null;
    }
}
