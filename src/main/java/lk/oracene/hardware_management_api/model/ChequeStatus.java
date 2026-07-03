package lk.oracene.hardware_management_api.model;

public enum ChequeStatus {
    PENDING,    // Cheque received, not yet deposited
    CLEARED,    // Successfully cleared by bank
    RETURNED,   // Returned due to insufficient funds or other issue
    CANCELLED   // Cancelled before processing
}
