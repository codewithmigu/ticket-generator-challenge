package com.migu.utils.messagegenerator;

import com.migu.model.Strip;
import com.migu.utils.Constants;

public class InvalidTicketsNumberMessageGenerator {
    public void generateInvalidTicketsNumberMessage(StringBuilder messageBuilder, Strip strip) {
        messageBuilder.append(String.format(Constants.REQUIREMENT_NOT_FULFILLED_MESSAGE, "A STRIP CONTAINS 6 TICKETS"));
        messageBuilder.append(String.format("Strip contains %s tickets. \n", strip.getTickets().size()));
    }
}
