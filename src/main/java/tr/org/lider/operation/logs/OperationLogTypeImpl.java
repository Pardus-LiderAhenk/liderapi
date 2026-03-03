package tr.org.lider.operation.logs;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "C_OPERATION_LOG_TYPE")
public class OperationLogTypeImpl implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPERATION_LOG_TYPE_ID")
    private Long id;

    @Schema(description = "operation log type", example = "EXECUTE_TASK")
    @Column(name = "TYPE", length = 50)
    private String type;

    @Schema(description = "Operation type ID from OperationType enum", example = "7")
    @Column(name = "OPERATION_TYPE_ID")
    private Integer operationTypeId;

    @Schema(description = "Description in Turkish", example = "Görev Çalıştırma")
    @Column(name = "DESCRIPTION_TR")
    private String descriptionTr;

    @Schema(description = "Description in English", example = "Execute Task")
    @Column(name = "DECRIPTION_EN")
    private String descriptionEn;

    @CreationTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "CREATE_DATE", nullable = false)
    private LocalDateTime createDate;
}
