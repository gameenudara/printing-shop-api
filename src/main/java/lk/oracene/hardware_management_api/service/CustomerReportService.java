package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.response.CustomerOutstandingReportResponse;

public interface CustomerReportService {
    CustomerOutstandingReportResponse getOutstandingReport();
}
