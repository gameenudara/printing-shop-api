package lk.oracene.hardware_management_api.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrinterSettingsResponse {

    private Long id;
    private String printerName;
    private Integer paperWidthDots;
    private Boolean autoCut;
    private String charset;
    private String headerText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
