package com.migu;

import com.migu.model.Position;
import com.migu.model.Ticket;
import com.migu.utils.validator.TicketValidator;
import com.migu.utils.validator.ValidatorException;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppTest {

    private final TicketValidator ticketValidator = new TicketValidator();

    private final Ticket mockTicket = generateMock(
            Stream.of(
                    Map.entry(Position.of(0, 0), 1),
                    Map.entry(Position.of(0, 1), 10),
                    Map.entry(Position.of(0, 2), 20),
                    Map.entry(Position.of(0, 3), 30),
                    Map.entry(Position.of(0, 4), 40),
                    Map.entry(Position.of(1, 4), 41),
                    Map.entry(Position.of(1, 5), 50),
                    Map.entry(Position.of(1, 6), 60),
                    Map.entry(Position.of(1, 7), 70),
                    Map.entry(Position.of(1, 8), 80),
                    Map.entry(Position.of(2, 0), 2),
                    Map.entry(Position.of(2, 1), 11),
                    Map.entry(Position.of(2, 2), 21),
                    Map.entry(Position.of(2, 3), 31),
                    Map.entry(Position.of(2, 4), 42)
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    );
    @Test
    public void test() {
        try {
            ticketValidator.validate(mockTicket);
        } catch (ValidatorException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * generates a ticket with values from @positionValuesMap if present, if not will add 0 on that position
     * @param positionValuesMap
     * @return
     */
    private Ticket generateMock(Map<Position, Integer> positionValuesMap) {
        final var ticketValuesMap = new HashMap<Position, Integer>();

        for (int column = 0; column < 9; column++) {
            for (int line = 0; line < 3; line++) {
                var position = Position.of(line, column);
                ticketValuesMap.put(position, positionValuesMap.getOrDefault(position, 0));
            }
        }

        return Ticket.of(ticketValuesMap);
    };

}
