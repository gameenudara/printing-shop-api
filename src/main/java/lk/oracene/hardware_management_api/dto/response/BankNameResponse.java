package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankNameResponse {

    private String value;
    private String label;

}
