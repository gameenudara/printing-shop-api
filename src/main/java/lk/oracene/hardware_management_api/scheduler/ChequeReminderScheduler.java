package lk.oracene.hardware_management_api.scheduler;

import lk.oracene.hardware_management_api.model.Cheque;
import lk.oracene.hardware_management_api.model.ChequeStatus;
import lk.oracene.hardware_management_api.model.ChequeType;
import lk.oracene.hardware_management_api.model.NotificationType;
import lk.oracene.hardware_management_api.repository.ChequeRepository;
import lk.oracene.hardware_management_api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChequeReminderScheduler {

    private final ChequeRepository chequeRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendChequeReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(3);

        List<Cheque> upcomingCheques = chequeRepository
                .findByChequeStatusAndDueDateLessThanEqual(ChequeStatus.PENDING, reminderDate);

        for (Cheque cheque : upcomingCheques) {
            if (cheque.getDueDate().isBefore(LocalDate.now())) {
                continue;
            }

            String message = buildMessage(cheque);
            notificationService.createNotification(
                    NotificationType.CHEQUE_DUE,
                    message,
                    cheque.getChequePaymentId());
        }
    }

    private String buildMessage(Cheque cheque) {
        if (cheque.getChequeType() == ChequeType.RECEIVED_FROM_CUSTOMER) {
            String name = cheque.getCustomer() != null ? cheque.getCustomer().getCustomerName() : "Unknown";
            return "Cheque #" + cheque.getChequeNumber() + " received from " + name
                    + " of " + cheque.getAmount() + " is due on " + cheque.getDueDate();
        } else {
            String name = cheque.getSupplier() != null ? cheque.getSupplier().getName() : "Unknown";
            return "Cheque #" + cheque.getChequeNumber() + " given to " + name
                    + " of " + cheque.getAmount() + " is due on " + cheque.getDueDate();
        }
    }
}
