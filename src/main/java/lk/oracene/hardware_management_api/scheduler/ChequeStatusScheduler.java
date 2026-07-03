package lk.oracene.hardware_management_api.scheduler;

import lk.oracene.hardware_management_api.model.Cheque;
import lk.oracene.hardware_management_api.model.ChequeStatus;
import lk.oracene.hardware_management_api.repository.ChequeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChequeStatusScheduler {

    private final ChequeRepository chequeRepository;

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoClearPendingCheques() {
        List<Cheque> dueCheques = chequeRepository
                .findByChequeStatusAndDueDateLessThanEqual(ChequeStatus.PENDING, LocalDate.now());
        dueCheques.forEach(cheque -> cheque.setChequeStatus(ChequeStatus.CLEARED));
        chequeRepository.saveAll(dueCheques);
    }
}
