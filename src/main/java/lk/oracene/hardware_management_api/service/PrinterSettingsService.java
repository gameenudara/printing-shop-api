package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.PrinterSettingsRequest;
import lk.oracene.hardware_management_api.dto.response.PrinterSettingsResponse;

public interface PrinterSettingsService {

    PrinterSettingsResponse getSettings();

    PrinterSettingsResponse updateSettings(PrinterSettingsRequest request);
}
