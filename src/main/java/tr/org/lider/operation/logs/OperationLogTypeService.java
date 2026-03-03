package tr.org.lider.operation.logs;

import org.springframework.stereotype.Service;
import tr.org.lider.entities.OperationType;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperationLogTypeService {
    private final OperationLogTypeRepository operationLogTypeRepository;

    public OperationLogTypeService(OperationLogTypeRepository operationLogTypeRepository) {
        this.operationLogTypeRepository = operationLogTypeRepository;
    }

    @PostConstruct
    public void init() {
        List<OperationLogTypeImpl> operationLogTypesList = new ArrayList<>();

        for (OperationType operationType : OperationType.values()) {
            String typeName = operationType.name();

            if (!operationLogTypeRepository.findByType(typeName).isPresent()) {
                OperationLogTypeImpl operationLogType = OperationLogTypeImpl.builder()
                        .type(typeName)
                        .operationTypeId(operationType.getId())
                        .descriptionEn(getDescriptionEn(operationType))
                        .descriptionTr(getDescriptionTr(operationType))
                        .build();

                operationLogTypesList.add(operationLogType);
            }
        }

        if (!operationLogTypesList.isEmpty()) {
            operationLogTypeRepository.saveAll(operationLogTypesList);
        }
    }

    private String getDescriptionTr(OperationType operationType) {
        switch (operationType) {
            case CREATE:
                return "Oluşturma";
            case READ:
                return "Okuma";
            case UPDATE:
                return "Güncelleme";
            case DELETE:
                return "Silme";
            case LOGIN:
                return "Oturum Açma";
            case LOGOUT:
                return "Çıkış";
            case EXECUTE_TASK:
                return "Görev Çalıştırma";
            case EXECUTE_POLICY:
                return "Politika Çalıştırma";
            case CHANGE_PASSWORD:
                return "Şifre Değiştirme";
            case MOVE:
                return "Taşıma";
            case UNASSIGMENT_POLICY:
                return "Politika Atamasını Kaldırma";
            case UPDATE_SCHEDULED_TASK:
                return "Zamanlanmış Görev Güncelleme";
            case CANCEL_SCHEDULED_TASK:
                return "Zamanlanmış Görev İptal Etme";
            case SEND_MAIL:
                return "E-posta Gönderme";
            case REGISTER:
                return "Kayıt";
            case UNREGISTER:
                return "Kayıt Silme";
            default:
                return operationType.name();
        }
    }

    private String getDescriptionEn(OperationType operationType) {
        switch (operationType) {
            case CREATE:
                return "Create";
            case READ:
                return "Read";
            case UPDATE:
                return "Update";
            case DELETE:
                return "Delete";
            case LOGIN:
                return "Login";
            case LOGOUT:
                return "Logout";
            case EXECUTE_TASK:
                return "Execute Task";
            case EXECUTE_POLICY:
                return "Execute Policy";
            case CHANGE_PASSWORD:
                return "Change Password";
            case MOVE:
                return "Move";
            case UNASSIGMENT_POLICY:
                return "Unassign Policy";
            case UPDATE_SCHEDULED_TASK:
                return "Update Scheduled Task";
            case CANCEL_SCHEDULED_TASK:
                return "Cancel Scheduled Task";
            case SEND_MAIL:
                return "Send Mail";
            case REGISTER:
                return "Register";
            case UNREGISTER:
                return "Unregister";
            default:
                return operationType.name();
        }
    }

    public List<OperationLogTypeImpl> listAll() {
        return operationLogTypeRepository.findAll();
    }
}