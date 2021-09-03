package com.app.google_sheets_service.service;

import com.app.api.GoogleSheetsRestService;
import com.app.api.model.BatchUpdateParams;
import com.app.api.model.ListCreationResponse;
import com.app.api.model.WriteRequestParams;
import com.app.api.model.WriteResponse;
import com.app.google_sheets_service.domain.GoogleTable;
import com.app.google_sheets_service.domain.GoogleTableRepository;
import com.app.google_sheets_service.domain.GoogleTableSheet;
import com.app.google_sheets_service.domain.GoogleTableSheetRepository;
import com.app.google_sheets_service.model.GoogleSheetsWriteRequest;
import com.app.google_sheets_service.model.SheetsPropertiesResponse;
import com.app.google_sheets_service.model.SheetsPropertiesResponse.SheetPropertiesEntity;
import com.app.google_sheets_service.model.TableCreationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            owner = authentication.getPrincipal().toString();
        GoogleTable table = getGoogleTable(request.getTableName(), owner);
        GoogleTableSheet sheet = getSheetForTable(table,
                request.getSheetName(),
                request.getConditionalRules());
        updateRange(request);
        clearSheet(sheet, request.getRange());
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
        if (request.getSheetFormat() != null)
            formatSheet(sheet, request.getSheetFormat());
        log.info("writeToCompanyTable({}, {}): the values are recorded", request.getTableName(),
                request.getSheetName());
        return new WriteResponse()
                .setTableUrl("https://docs.google.com/spreadsheets/d/" + table.getSpreadSheetId() + "/edit#gid=" + sheet.getSheetId());
    }

    private void updateRange(GoogleSheetsWriteRequest request) {
        List<List<String>> values = request.getValues();
        int rowsNumber = values.size();
        AtomicInteger columnsNumber = new AtomicInteger(1);
        values.forEach(v -> columnsNumber.set(Math.max(v.size(), columnsNumber.get())));

        char beginColumn = 'A';
        int beginRow = 1;
        if (request.getRange() != null && !request.getRange().isEmpty()) {
            String rangeBegin = request.getRange().split(":")[0];
            int rowNumberIndex = IntStream.range(0, rangeBegin.length())
                    .filter(i -> rangeBegin.charAt(i) > 47 && rangeBegin.charAt(i) <= 57)
                    .findFirst().orElseThrow(() -> new ValidationException("Range is invalid"));
            beginColumn = rangeBegin.charAt(0);
            beginRow = Integer.parseInt(rangeBegin.substring(rowNumberIndex));
        }
        request.setRange((beginColumn + "" + beginRow) + ":" +
                ((char) (beginColumn + columnsNumber.get())) + "" + (beginRow + rowsNumber));
    }

    /**
     * В случае отсутствия в БД создаёт новый с добавлением условного форматирования
     * @param conditionalRules условные правила форматирования
     */
    private GoogleTableSheet getSheetForTable(GoogleTable table,
                                              String sheetName,
                                              String conditionalRules){
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
                    if (conditionalRules != null) formatSheet(newSheet, conditionalRules);
                    return newSheet;
                });
    }

    /**
     * Если ещё не создана, то создаёт новую и записывает информацию о ней в БД
     * @return таблица
     */
    private GoogleTable getGoogleTable(String tableName, String owner) {
        return googleTableRepository.findByTableNameAndOwner(tableName, owner).orElseGet(() -> {
            TableCreationResponse response = restService.createTable(tableName);
            assert response != null;
            GoogleTable googleTable = new GoogleTable(null, response.getSpreadSheetId(), tableName, owner, null);
            googleTableRepository.save(googleTable);
            return googleTable;
        });
    }

    private void clearSheet(GoogleTableSheet sheet, String range){
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
                .setRequests(Collections.singletonList(sheetFormat.replace("SHEET_ID", sheet.getSheetId().toString())));
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

    public void updateSheets(GoogleTable table, String sheetName){
        SheetsPropertiesResponse response = restService.getSheetsProperties(table.getSpreadSheetId());
        List<SheetPropertiesEntity> sheetProperties = new ArrayList<>(response.getSheets());
        sheetProperties.forEach(sheet -> {
            if (sheet.getProperties().getTitle().equals("Лист1")) {
                BatchUpdateParams batchUpdateParams = new BatchUpdateParams()
                        .setSpreadSheetId(table.getSpreadSheetId())
                        .setRequests(Collections.singletonList("    {" +
                                "      \"updateSheetProperties\": {" +
                                "        \"properties\": {" +
                                "           \"sheetId\": " + sheet.getProperties().getSheetId() + "," +
                                "           \"title\": \"" + sheetName + "\"" +
                                "          }," +
                                "          \"fields\": \"title\"" +
                                "      }" +
                                "    }"));
                restService.batchUpdate(batchUpdateParams, SheetsPropertiesResponse.class);
                sheet.getProperties().setTitle(sheetName);
            }
            GoogleTableSheet googleTableSheet = googleTableSheetRepository.findByTableAndSheetId(table, sheet.getProperties().getSheetId())
                    .orElse(new GoogleTableSheet(null, table, sheet.getProperties().getSheetId(), sheet.getProperties().getTitle()));
            googleTableSheet.setSheetName(sheet.getProperties().getTitle());
            googleTableSheetRepository.save(googleTableSheet);
        });
    }
}
