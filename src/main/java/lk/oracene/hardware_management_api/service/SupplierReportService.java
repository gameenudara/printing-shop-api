package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.response.SupplierOutstandingReportResponse;

public interface SupplierReportService {
    SupplierOutstandingReportResponse getOutstandingReport();
}
