package com.app.service.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleTableSheetRepository extends JpaRepository<GoogleTableSheet, Long> {

    void deleteByTableAndSheetId(GoogleTable googleTable, Long sheetId);

    Optional<GoogleTableSheet> findByTableAndSheetName(GoogleTable table, String name);

    Optional<GoogleTableSheet> findByTableAndSheetId(GoogleTable table, int sheetId);

}