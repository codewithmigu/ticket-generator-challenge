package com.migu.utils.validator;

import com.migu.model.Position;
import com.migu.model.Ticket;
import com.migu.utils.Constants;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TicketValidator implements Validator<Ticket> {
    private final StringBuilder errorMessageBuilder = new StringBuilder();
    private final Map<Integer, Integer> lineValuesCounterMap = new HashMap<>();
    private final Map<Integer, Integer> columnValuesCounterMap = new HashMap<>();
    private final Map<Integer, Map.Entry<Position, Integer>> columnBiggestValueTrackingMap = new HashMap<>();
    private final SortedSet<Integer> notOrderedColumnSet = new TreeSet<>();
    private final Map<Integer, Integer> lineSizeCounterMap = new HashMap<>();
    private final Map<Integer, Integer> columnSizeCounterMap = new HashMap<>();
    private final Map<Integer, List<Integer>> columnsWithOutOfRangeNumbers = new HashMap<>();
    private int valuesNumber = 0;
    private int emptySpacesNumber = 0;

    private final Predicate<Integer> IS_BLANK_SPACE = e -> e == 0;

    private final List<Consumer<Map.Entry<Position, Integer>>> TICKET_COMPUTATION_FUNCTIONS = List.of(
            e -> computeValueCounters(lineValuesCounterMap, e, () -> e.getKey().getLine()),
            e -> computeSize(lineSizeCounterMap, () -> e.getKey().getLine()),
            e -> computeValueCounters(columnValuesCounterMap, e, () -> e.getKey().getColumn()),
            e -> computeSize(columnSizeCounterMap, () -> e.getKey().getColumn()),
            e -> emptySpacesNumber = getIncrementedIfTrue(IS_BLANK_SPACE, e, emptySpacesNumber),
            e -> valuesNumber = getIncrementedIfTrue(IS_BLANK_SPACE.negate(), e, valuesNumber),
            this::checkAndTrackOrderedColumnRequirement,
            this::computeOutOfRangeNumbers
    );

    @Override
    public void validate(Ticket ticket) throws ValidatorException {
        ticket.getPositionValueMap()
                .entrySet()
                .forEach(e -> TICKET_COMPUTATION_FUNCTIONS.forEach(f -> f.accept(e)));

        // TODO: move errorMessageGenerators in other class
        if (valuesNumber != 15) {
            errorMessageBuilder.append(String.format(Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE, "NUMBERS PRESENT IN THE TICKET SHOULD BE 15"));
            errorMessageBuilder.append(String.format("Actual value of the number present on the ticket is %s.\n", valuesNumber));
        }
        if (emptySpacesNumber != 12) {
            errorMessageBuilder.append(String.format(Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE, "BLANK SPACES PRESENT SHOULD BE 12"));
            errorMessageBuilder.append(String.format("Actual value of the blank spaces present on the ticket is %s.\n", emptySpacesNumber));
        }

        var lineSize = getMapKeysSize(lineSizeCounterMap);
        if (lineSize != 3) {
            errorMessageBuilder.append(String.format(Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE, "TICKET LINE SIZE IS 3"));
            errorMessageBuilder.append(String.format("Actual value of the line size is %s\n", lineSize));
        }

        var columnSize = getMapKeysSize(columnSizeCounterMap);
        if (columnSize != 9) {
            errorMessageBuilder.append(String.format(Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE, "TICKET COLUMN SIZE IS 9"));
            errorMessageBuilder.append(String.format("Actual value of the column size is %s\n", columnSize));
        }

        Set<Integer> linesGreaterThanMaximum = getKeysGreaterThan(lineSizeCounterMap, 2);
        if (!linesGreaterThanMaximum.isEmpty()) {
            errorMessageBuilder.append(String.format(Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE, "TICKET LINE SIZE IS 3"));
            errorMessageBuilder.append(String.format("Following set of lines are greater than maximum allowed (three): %s\n", linesGreaterThanMaximum));
        }

        Set<Integer> columnsGreaterThanMaximum = getKeysGreaterThan(columnSizeCounterMap, 8);
        if (!columnsGreaterThanMaximum.isEmpty()) {
            errorMessageBuilder.append(String.format(Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE, "TICKET LINE SIZE IS 9"));
            errorMessageBuilder.append(String.format("Following set of columns are greater than maximum allowed (three): %s\n", columnsGreaterThanMaximum));
        }

        if (!notOrderedColumnSet.isEmpty()) {
            errorMessageBuilder.append(String.format(Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE, "NUMBERS IN THE TICKET COLUMNS SHOULD BE IN ASCENDING ORDER"));
            notOrderedColumnSet.forEach(v -> errorMessageBuilder.append(String.format("Numbers of column %s are not in ascending order\n", v)));
        }

        // TODO: Maybe different approach here in the method
        var fullBlankColumns = IntStream.range(0, 9)
                .filter(i -> IS_BLANK_SPACE.test(columnValuesCounterMap.getOrDefault(i, 0)))
                .boxed()
                .collect(Collectors.toUnmodifiableSet());

        if (!fullBlankColumns.isEmpty()) {
            errorMessageBuilder.append(String.format(Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE, "EACH TICKET COLUMN CONSISTS OF 1-3 NUMBERS AND NEVER THREE BLANKS"));
            errorMessageBuilder.append(String.format("Following columns contains three blanks: %s\n", fullBlankColumns));
        }

        var nonCompliantLines = lineValuesCounterMap.entrySet()
                .stream()
                .filter(e -> e.getValue() != 5)
                .collect(Collectors.toUnmodifiableSet());

        if (!nonCompliantLines.isEmpty()) {
            errorMessageBuilder.append(String.format(Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE, "EACH TICKET ROW CONTAINS FIVE NUMBERS AND FOUR BLANK SPACES"));
            nonCompliantLines.forEach(e ->
                    errorMessageBuilder.append(String.format("Line %s contains %s numbers and %s spaces.\n", e.getKey(), e.getValue(), 9 - e.getValue())));
        }

        if (!columnsWithOutOfRangeNumbers.isEmpty()) {
            errorMessageBuilder.append(String.format(Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE, "NUMBER FROM EACH COLUMN SHOULD BE WITHIN A RANGE"));
            columnsWithOutOfRangeNumbers.forEach((key, value) ->
                    errorMessageBuilder.append(String.format("Column %s contains following numbers not within %s and %s range: %s\n",
                            key, getColumnMinLimit(key), getColumnMaxLimit(key), value)));
        }

        if (!errorMessageBuilder.isEmpty()) {
            String errorMessage = errorMessageBuilder.toString();
            errorMessageBuilder.setLength(0);
            throw new ValidatorException(errorMessage);
        }
    }

    private void computeOutOfRangeNumbers(Map.Entry<Position, Integer> e) {
        var column = e.getKey().getColumn();
        var minLimit = getColumnMinLimit(column);
        var maxLimit = getColumnMaxLimit(column);
        var numberValue = e.getValue();

        if (!IS_BLANK_SPACE.test(numberValue) && (numberValue < minLimit || numberValue > maxLimit)) {
            columnsWithOutOfRangeNumbers.merge(column, List.of(numberValue), (entries1, entries2) ->
                    Stream.of(entries1, entries2)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toUnmodifiableList()));
        }
    }

    private int getColumnMinLimit(int column) {
        return column * 10;
    }

    private int getColumnMaxLimit(int column) {
        return column == 8 ? 90 : column * 10 + 9;
    }

    private int getMapKeysSize(Map<Integer, Integer> map) {
        return map.keySet()
                .size();
    }

    private Set<Integer> getKeysGreaterThan(Map<Integer, Integer> map, int range) {
        return map.keySet()
                .stream()
                .filter(k -> k > range)
                .collect(Collectors.toUnmodifiableSet());
    }

    private void computeSize(Map<Integer, Integer> sizeCounterMap, Supplier<Integer> supplier) {
        sizeCounterMap.put(supplier.get(), sizeCounterMap.getOrDefault(supplier.get(), 0));
    }

    private int getIncrementedIfTrue(Predicate<Integer> predicate, Map.Entry<Position, Integer> entry, int counter) {
        if (predicate.test(entry.getValue())) {
            return counter + 1;
        }
        return counter;
    }

    private void computeValueCounters(Map<Integer, Integer> valuesCounterMap, Map.Entry<Position, Integer> entry, Supplier<Integer> supplier) {
        if (IS_BLANK_SPACE.negate().test(entry.getValue())) {
            var counter = valuesCounterMap.getOrDefault(supplier.get(), 0);
            valuesCounterMap.put(supplier.get(), ++counter);
        }
    }

    private void checkAndTrackOrderedColumnRequirement(Map.Entry<Position, Integer> entry) {
        Map.Entry<Position, Integer> biggestTrackedColumnEntry = columnBiggestValueTrackingMap.get(entry.getKey().getColumn());

        if (biggestTrackedColumnEntry != null && IS_BLANK_SPACE.negate().test(biggestTrackedColumnEntry.getValue())) {
            boolean isSuccessive = entry.getKey().getLine() > biggestTrackedColumnEntry.getKey().getLine() &&
                    Objects.equals(biggestTrackedColumnEntry.getKey().getColumn(), entry.getKey().getColumn());

            if ((entry.getValue() < biggestTrackedColumnEntry.getValue() && isSuccessive) ||
                    (entry.getValue() > biggestTrackedColumnEntry.getValue() && !isSuccessive)) {
                notOrderedColumnSet.add(entry.getKey().getColumn());
            }
        }

        columnBiggestValueTrackingMap.put(entry.getKey().getColumn(), entry);
    }
}
