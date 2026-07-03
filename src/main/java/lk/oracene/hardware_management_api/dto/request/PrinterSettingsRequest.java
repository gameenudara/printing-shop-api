package lk.oracene.hardware_management_api.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrinterSettingsRequest {

    private String printerName;
    private Integer paperWidthDots;
    private Boolean autoCut;
    private String charset;
    private String headerText;
}
