package com.app.google_sheets_service.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GoogleSheetsWriteRequest {
    @NonNull
    private String tableName;
    @NonNull
    private String sheetName;
    @NonNull
    private List<List<String>> values;
    /**
     * Ячейка начала диапазона записи
     */
    private String range;
    /**
     * Форматирование листа электронной таблицы
     */
    @Nullable
    private List<JsonNode> sheetFormat;
    /**
     * Условное форматирование листа.
     * Применяется только при создании листа
     */
    @Nullable
    private List<JsonNode> conditionalRules;

    @Nullable
    public String getSheetFormat() {
        return sheetFormat != null ? sheetFormat.stream()
                .map(JsonNode::toString)
                .collect(Collectors.joining(",")) : null;
    }

    @Nullable
    public String getConditionalRules() {
        return conditionalRules != null ? conditionalRules.stream()
                .map(JsonNode::toString)
                .collect(Collectors.joining(",")) : null;
    }
}
