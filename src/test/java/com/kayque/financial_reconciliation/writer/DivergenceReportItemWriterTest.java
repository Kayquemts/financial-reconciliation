package com.kayque.financial_reconciliation.writer;

import com.kayque.financial_reconciliation.batch.writer.DivergenceReportItemWriter;
import com.kayque.financial_reconciliation.domain.dto.ReconciliationResultDTO;
import com.kayque.financial_reconciliation.domain.enums.ReconciliationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DivergenceReportItemWriterTest {

    @Mock
    private FlatFileItemWriter<ReconciliationResultDTO> delegate;

    private DivergenceReportItemWriter writer;

    @Test
    void shouldForwardOnlyNonReconciledItemsToDelegate() throws Exception {
        writer = new DivergenceReportItemWriter(delegate);

        ReconciliationResultDTO reconciled = resultFor("TRX-001", ReconciliationStatus.RECONCILED);
        ReconciliationResultDTO amountMismatch = resultFor("TRX-002", ReconciliationStatus.AMOUNT_MISMATCH);
        ReconciliationResultDTO notFound = resultFor("TRX-003", ReconciliationStatus.NOT_FOUND);

        writer.write(new Chunk<>(List.of(reconciled, amountMismatch, notFound)));

        ArgumentCaptor<Chunk<ReconciliationResultDTO>> captor = ArgumentCaptor.forClass(Chunk.class);
        verify(delegate).write(captor.capture());

        List<ReconciliationResultDTO> forwarded = captor.getValue().getItems();
        assertThat(forwarded).hasSize(2);
        assertThat(forwarded).extracting(ReconciliationResultDTO::getTransactionId)
                .containsExactlyInAnyOrder("TRX-002", "TRX-003");
        assertThat(forwarded).noneMatch(item -> item.getResult() == ReconciliationStatus.RECONCILED);
    }

    @Test
    void shouldNotCallDelegateWhenAllItemsAreReconciled() throws Exception {
        writer = new DivergenceReportItemWriter(delegate);

        writer.write(new Chunk<>(List.of(
                resultFor("TRX-001", ReconciliationStatus.RECONCILED),
                resultFor("TRX-002", ReconciliationStatus.RECONCILED)
        )));

        verify(delegate, never()).write(any());
    }

    @Test
    void shouldDelegateStreamLifecycleCalls(){
        writer = new DivergenceReportItemWriter(delegate);
        ExecutionContext context = new ExecutionContext();

        writer.open(context);
        writer.update(context);
        writer.close();

        verify(delegate).open(context);
        verify(delegate).update(context);
        verify(delegate).close();
    }

    private ReconciliationResultDTO resultFor(String transactionId, ReconciliationStatus status) {
        return new ReconciliationResultDTO(
                transactionId,
                new BigDecimal("150.00"),
                new BigDecimal("150.00"),
                "PENDING",
                "APPROVED",
                status,
                "test observation",
                LocalDateTime.now()
        );
    }
}