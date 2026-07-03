package lk.oracene.hardware_management_api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankName {

    BANK_OF_CEYLON("Bank of Ceylon"),
    PEOPLES_BANK("Peoples Bank"),
    COMMERCIAL_BANK("Commercial Bank"),
    HATTON_NATIONAL_BANK("Hatton National Bank"),
    SAMPATH_BANK("Sampath Bank"),
    NATIONS_TRUST_BANK("Nations Trust Bank"),
    SEYLAN_BANK("Seylan Bank"),
    NATIONAL_DEVELOPMENT_BANK("National Development Bank"),
    PAN_ASIA_BANK("Pan Asia Bank"),
    UNION_BANK("Union Bank"),
    DFCC_BANK("DFCC Bank"),
    AMANA_BANK("Amana Bank"),
    CARGILLS_BANK("Cargills Bank"),
    SANASA_BANK("SANASA Bank"),
    HSBC("HSBC"),
    STANDARD_CHARTERED_BANK("Standard Chartered Bank"),
    CITIBANK("Citibank"),
    STATE_MORTGAGE_AND_INVESTMENT_BANK("State Mortgage and Investment Bank"),
    REGIONAL_DEVELOPMENT_BANK("Regional Development Bank"),
    NSB("NSB"),
    HDFC_BANK("HDFC Bank"),
    LOLC_FINANCE("LOLC Finance"),
    CENTRAL_FINANCE("Central Finance"),
    LB_FINANCE("LB Finance"),
    SOFTLOGIC_FINANCE("Softlogic Finance");

    private final String label;

}
