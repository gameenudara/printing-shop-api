package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "printer_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PrinterSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "printer_name")
    private String printerName;

    @Column(name = "paper_width_dots", nullable = false)
    private Integer paperWidthDots = 576;

    @Column(name = "auto_cut", nullable = false)
    private Boolean autoCut = true;

    @Column(name = "charset", nullable = false, length = 20)
    private String charset = "CP437";

    @Column(name = "header_text")
    private String headerText;
}
