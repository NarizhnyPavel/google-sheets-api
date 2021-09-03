package com.app.google_sheets_service.service;

import com.app.api.model.WriteResponse;
import com.app.google_sheets_service.model.GoogleSheetsWriteRequest;

public interface GoogleSheetsService {

    WriteResponse writeToCompanyTable(GoogleSheetsWriteRequest googleSheetsWriteRequest);

}
