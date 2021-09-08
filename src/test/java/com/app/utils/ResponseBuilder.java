package com.app.utils;

import com.app.service.model.GoogleSheetsWriteRequest;
import com.app.service.model.WriteResponse;

import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class ResponseBuilder {

    public static WriteResponse buildResponse(){
        return new WriteResponse().setTableUrl(randomAlphabetic(20));
    }

    public static GoogleSheetsWriteRequest buildRequest() {
        return new GoogleSheetsWriteRequest()
                .setTableName(randomAlphabetic(10))
                .setSheetName(randomAlphabetic(10))
                .setRange("B2")
                .setValues(Collections.singletonList(Collections.singletonList(randomAlphabetic(5))))
                .setSheetFormat(buildFormat())
                .setConditionalRules(buildFormat());
    }

    private static List<String> buildFormat() {
        return Collections.singletonList("{" +
                        "\"" + randomAlphabetic(5) + "\": " + "\"" + randomAlphabetic(5) + "\"" +
        "}");
    }

    private static List<String> buildBadFormat() {
        return Collections.singletonList(randomAlphabetic(5));
    }
}
