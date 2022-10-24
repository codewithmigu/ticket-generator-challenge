package com.migu.utils.validator;

import com.migu.model.Strip;
import com.migu.model.Ticket;
import com.migu.utils.messagegenerator.DuplicateValuesMessageGenerator;
import com.migu.utils.messagegenerator.InvalidTicketsNumberMessageGenerator;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StripValidator implements Validator<Strip> {
    private TicketValidator ticketValidator;
    private StringBuilder errorMessageBuilder = new StringBuilder();
    private final Map<Integer, Integer> appearancesCounterMap = new HashMap<>();

    private final List<Consumer<Strip>> STRIP_CHECK_FUNCTIONS = List.of(
            this::checkBingoTicket,
            this::checkNumberOfTickets,
            this::checkForDuplicates
    );

    @Override
    public void validate(Strip strip) throws ValidatorException {
        STRIP_CHECK_FUNCTIONS.forEach(f -> f.accept(strip));

        if (!errorMessageBuilder.isEmpty()) {
            String errorMessage = errorMessageBuilder.toString();
            errorMessageBuilder.setLength(0);
            throw new ValidatorException(errorMessage);
        }
    }

    private void checkBingoTicket(Strip strip) {
        strip.getTickets().forEach(ticket -> {
            try {
                ticketValidator.validate(ticket);
            } catch (ValidatorException e) {
                errorMessageBuilder.append(e.getMessage());
            }
        });
    }

    private void checkNumberOfTickets(Strip strip) {
        if (strip.getTickets() == null || strip.getTickets().size() != 6) {
            InvalidTicketsNumberMessageGenerator messageGenerator = new InvalidTicketsNumberMessageGenerator();
            messageGenerator.generateInvalidTicketsNumberMessage(errorMessageBuilder, strip);
        }
    }

    private void checkForDuplicates(Strip strip) {
        Set<Integer> duplicateValues = findDuplicatesFrom(strip);

        if (!duplicateValues.isEmpty()) {
            DuplicateValuesMessageGenerator duplicateValuesMessageGenerator = new DuplicateValuesMessageGenerator();
            errorMessageBuilder.append(duplicateValuesMessageGenerator.generateErrorMessage(errorMessageBuilder, duplicateValues, appearancesCounterMap));
        }
    }

    private Set<Integer> findDuplicatesFrom(Strip strip) {
        return strip
                .getTickets()
                .stream()
                .map(this::findDuplicatesFrom)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<Integer> findDuplicatesFrom(Ticket ticket) {
        return ticket.getPositionValueMap()
                .values()
                .stream()
                .filter(value -> value.equals(0))
                .peek(this::calculateAppearances)
                .filter(this::isPresentMoreThanOnce)
                .collect(Collectors.toUnmodifiableSet());
    }

    private void calculateAppearances(Integer value) {
        appearancesCounterMap.put(value, appearancesCounterMap.getOrDefault(value, 0) + 1);
    }

    private boolean isPresentMoreThanOnce(Integer integer) {
        return appearancesCounterMap.get(integer) > 1;
    }
}
