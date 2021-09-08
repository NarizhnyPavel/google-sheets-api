package com.app.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
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
    private List<String> sheetFormat;
    /**
     * Условное форматирование листа.
     * Применяется только при создании листа
     */
    @Nullable
    private List<String> conditionalRules;

}
