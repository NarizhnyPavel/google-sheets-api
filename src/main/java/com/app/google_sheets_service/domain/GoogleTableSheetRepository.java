package com.app.google_sheets_service.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleTableSheetRepository extends JpaRepository<GoogleTableSheet, Long> {

    void deleteByTableAndSheetId(GoogleTable googleTable, Long sheetId);

    Optional<GoogleTableSheet> findByTableAndSheetName(GoogleTable table, String name);

    Optional<GoogleTableSheet> findByTableAndSheetId(GoogleTable table, Long sheetId);

}