package com.app.service.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GoogleTableRepository extends JpaRepository<GoogleTable, Long> {

    @Query(value = "select g.sheets from GoogleTable g where g.id = :id")
    Optional<List<GoogleTableSheet>> getSheetsById(@Param("id") Long id);

    Optional<GoogleTable> findByTableNameAndOwner(String tableName, String owner);

}