package com.app.service;

import com.app.service.model.GoogleSheetsWriteRequest;
import com.app.service.model.WriteResponse;

public interface GoogleSheetsService {

    WriteResponse writeToTable(GoogleSheetsWriteRequest googleSheetsWriteRequest);

}
