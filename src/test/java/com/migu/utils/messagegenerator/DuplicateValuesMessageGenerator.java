package com.migu.utils.messagegenerator;

import java.util.Map;
import java.util.Set;

import static com.migu.utils.Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE;

public class DuplicateValuesMessageGenerator {

    public StringBuilder generateErrorMessage(StringBuilder messageBuilder, Set<Integer> element, Map<Integer, Integer> appearancesMap) {
        messageBuilder
                .append(String.format(REQUIREMENT_NOT_FULFILLED_MESSAGE, "NUMBERS PRESENT ON A STRIP SHOULD BE UNIQUE"))
                .append("Following elements are found more than once:\n");
        element.forEach(v -> generateMessageForDuplicateElement(v, appearancesMap.get(v), messageBuilder));

        return messageBuilder;
    }

    private void generateMessageForDuplicateElement(Integer value, Integer appearancesNumber, StringBuilder messageBuilder) {
        messageBuilder.append(String.format("Value %s found %s times in a bingo strip.\n", value, appearancesNumber));
    }
}
