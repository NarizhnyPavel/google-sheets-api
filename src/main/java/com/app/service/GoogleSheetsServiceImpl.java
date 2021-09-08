package com.app.service;

import com.app.api.spreadsheets.GoogleSheetsRestService;
import com.app.api.spreadsheets.model.*;
import com.app.service.domain.GoogleTable;
import com.app.service.domain.GoogleTableRepository;
import com.app.service.domain.GoogleTableSheet;
import com.app.service.domain.GoogleTableSheetRepository;
import com.app.service.model.GoogleSheetsWriteRequest;
import com.app.service.model.WriteResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.app.api.spreadsheets.model.SheetsPropertiesResponse.SheetPropertiesEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

    private final GoogleTableRepository googleTableRepository;
    private final GoogleTableSheetRepository googleTableSheetRepository;
    private final GoogleSheetsRestService restService;

    @Override
    public WriteResponse writeToCompanyTable(GoogleSheetsWriteRequest request) {
        log.info("writeToCompanyTable({}, {})", request.getTableName(),
                request.getSheetName());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String owner = null;
        if (authentication != null && !authentication.getPrincipal().toString().equals("anonymousUser"))
            owner = authentication.getName();
        GoogleTable table = getGoogleTable(request.getTableName(), owner);
        GoogleTableSheet sheet = getSheetForTable(table,
                request.getSheetName(),
                request.getConditionalRules());
        updateRange(request);
        clearRange(sheet, request.getRange());
        WriteRequestParams writeRequest = new WriteRequestParams()
                .setSpreadSheetId(table.getSpreadSheetId())
                .setRange("'" + sheet.getSheetName() + "'!" + request.getRange())
                .setValues(request.getValues().stream()
                        .map(e -> e.stream()
                                .map(v -> "\"" + (v == null ? "" : v) + "\"")
                                .collect(Collectors.toList())
                                .toString())
                        .collect(Collectors.toList()));
        restService.writeToSpreadSheet(writeRequest);
        if (request.getSheetFormat() != null) {
            validateFormat(request.getSheetFormat());
            formatSheet(sheet, String.join(",", request.getSheetFormat()));
        }
        log.info("writeToCompanyTable({}, {}): the values are recorded", request.getTableName(),
                request.getSheetName());
        return new WriteResponse()
                .setTableUrl("https://docs.google.com/spreadsheets/d/" + table.getSpreadSheetId() + "/edit#gid=" + sheet.getSheetId());
    }

    private void validateFormat(List<String> values) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readTree(values.toString());
        } catch (JsonProcessingException e) {
            throw new ValidationException("validation exception: " + e.getMessage());
        }
    }

    private void updateRange(GoogleSheetsWriteRequest request) {
        List<List<String>> values = request.getValues();
        int rowsNumber = values.size();
        AtomicInteger columnsNumber = new AtomicInteger(1);
        values.forEach(v -> columnsNumber.set(Math.max(v.size(), columnsNumber.get())));

        char beginColumn = 'A';
        int beginRow = 0;
        if (request.getRange() != null && !request.getRange().isEmpty()) {
            String rangeBegin = request.getRange().split(":")[0];
            int rowNumberIndex = IntStream.range(0, rangeBegin.length())
                    .filter(i -> rangeBegin.charAt(i) > 47 && rangeBegin.charAt(i) <= 57)
                    .findFirst().orElseThrow(() -> new ValidationException("Range is invalid"));
            beginColumn = rangeBegin.charAt(0);
            beginRow = Integer.parseInt(rangeBegin.substring(rowNumberIndex));
        }
        request.setRange((beginColumn + "" + beginRow) + ":" +
                ((char) (beginColumn + (columnsNumber.get() - 1))) + "" + (beginRow + (rowsNumber - 1)));
    }

    /**
     * В случае отсутствия в БД создаёт новый с добавлением условного форматирования
     * @param conditionalRules условные правила форматирования
     */
    private GoogleTableSheet getSheetForTable(GoogleTable table,
                                              String sheetName,
                                              List<String> conditionalRules){
        updateSheets(table, sheetName);
        return googleTableSheetRepository
                .findByTableAndSheetName(table, sheetName)
                .orElseGet(() -> {
                    BatchUpdateParams batchUpdateParams = new BatchUpdateParams()
                            .setSpreadSheetId(table.getSpreadSheetId())
                            .setRequests(Collections.singletonList("    {" +
                                    "      \"addSheet\": {" +
                                    "        \"properties\": {" +
                                    "          \"title\": \"" + sheetName + "\"," +
                                    "          \"gridProperties\": {" +
                                    "            \"rowCount\": 1000," +
                                    "            \"columnCount\": 100" +
                                    "          }" +
                                    "        }" +
                                    "      }" +
                                    "    }"));
                    ListCreationResponse response = restService.batchUpdate(batchUpdateParams, ListCreationResponse.class);
                    GoogleTableSheet newSheet = new GoogleTableSheet(null, table, response.getSheetId(), sheetName);
                    googleTableSheetRepository.save(newSheet);
                    moveSheetToZeroIndex(newSheet);
                    if (conditionalRules != null) {
                        validateFormat(conditionalRules);
                        formatSheet(newSheet, String.join(",", conditionalRules));
                    }
                    return newSheet;
                });
    }

    private GoogleTable getGoogleTable(String tableName, String owner) {
        return googleTableRepository.findByTableNameAndOwner(tableName, owner).orElseGet(() -> {
            TableCreationResponse response = restService.createTable(tableName);
            assert response != null;
            GoogleTable googleTable = new GoogleTable(null, response.getSpreadSheetId(), tableName, owner, null);
            googleTableRepository.save(googleTable);
            return googleTable;
        });
    }

    private void clearRange(GoogleTableSheet sheet, String range){
        String[] parts = range.split(":");
        int firstInt = IntStream.range(0, parts[0].length())
                .filter(i -> parts[0].charAt(i) >= 47 && parts[0].charAt(i) <= 57)
                .findFirst().orElseThrow(() -> new ValidationException("Range is invalid"));
        int firstRaw = Integer.parseInt(parts[0].substring(firstInt)) - 1;
        int firstColumn = 0;
        for (int i = 0; i < parts[0].substring(0, firstInt).length(); i++)
            firstColumn +=  ((int) parts[0].substring(0, firstInt).charAt(i)) - 65;
        firstInt = IntStream.range(0, parts[1].length())
                .filter(i -> parts[1].charAt(i) >= 47 && parts[1].charAt(i) <= 57)
                .findFirst().orElseThrow(() -> new ValidationException("Range is invalid"));
        int lastRaw = Integer.parseInt(parts[1].substring(firstInt)) - 1;
        int lastColumn = 0;
        for (int i = 0; i < parts[0].substring(0, firstInt).length(); i++)
            lastColumn += ((int) parts[1].substring(0, firstInt).charAt(i)) - 65;
        BatchUpdateParams batchUpdateParams = new BatchUpdateParams()
                .setSpreadSheetId(sheet.getTable().getSpreadSheetId())
                .setRequests(Collections.singletonList("   {" +
                        "      \"updateCells\": {" +
                        "        \"range\": {" +
                        "          \"sheetId\": " + sheet.getSheetId() + "," +
                        "          \"startColumnIndex\": \"" + firstColumn + "\"," +
                        "          \"endColumnIndex\":\"" + lastColumn + "\"," +
                        "          \"startRowIndex\": " + firstRaw + "," +
                        "          \"endRowIndex\":" + lastRaw +
                        "        }," +
                        "         \"fields\": \"userEnteredFormat, userEnteredValue\"" +
                        "      }" +
                        "   }"));
        restService.batchUpdate(batchUpdateParams, Object.class);
    }

    private void formatSheet(GoogleTableSheet sheet, String sheetFormat){
        BatchUpdateParams batchUpdateParams = new BatchUpdateParams()
                .setSpreadSheetId(sheet.getTable().getSpreadSheetId())
                .setRequests(Collections.singletonList(sheetFormat.replace("SHEET_ID", String.valueOf(sheet.getSheetId()))));
        restService.batchUpdate(batchUpdateParams, Object.class);
    }

    private void moveSheetToZeroIndex(GoogleTableSheet sheet){
        BatchUpdateParams batchUpdateParams = new BatchUpdateParams()
                .setSpreadSheetId(sheet.getTable().getSpreadSheetId())
                .setRequests(Collections.singletonList("    {" +
                        "      \"updateSheetProperties\": {" +
                        "        \"properties\": {" +
                        "          \"sheetId\": " + sheet.getSheetId() + "," +
                        "          \"index\": 0" +
                        "        }," +
                        "        \"fields\": \"index\"" +
                        "      }" +
                        "    }"));
        restService.batchUpdate(batchUpdateParams, Object.class);
    }

    public void updateSheets(GoogleTable table, String sheetName) {
        List<SheetPropertiesEntity> sheetProperties = restService.getSheetsProperties(table.getSpreadSheetId());
        sheetProperties.forEach(sheet -> {
            if (sheet.getTitle().equals("Лист1")) {
                BatchUpdateParams batchUpdateParams = new BatchUpdateParams()
                        .setSpreadSheetId(table.getSpreadSheetId())
                        .setRequests(Collections.singletonList("    {" +
                                "      \"updateSheetProperties\": {" +
                                "        \"properties\": {" +
                                "           \"sheetId\": " + sheet.getSheetId() + "," +
                                "           \"title\": \"" + sheetName + "\"" +
                                "          }," +
                                "          \"fields\": \"title\"" +
                                "      }" +
                                "    }"));
                restService.batchUpdate(batchUpdateParams, SheetsPropertiesResponse.class);
                sheet.setTitle(sheetName);
            }
            GoogleTableSheet googleTableSheet = googleTableSheetRepository.findByTableAndSheetId(table, sheet.getSheetId())
                    .orElse(new GoogleTableSheet(null, table, sheet.getSheetId(), sheet.getTitle()));
            googleTableSheet.setSheetName(sheet.getTitle());
            googleTableSheetRepository.save(googleTableSheet);
        });
    }
}
