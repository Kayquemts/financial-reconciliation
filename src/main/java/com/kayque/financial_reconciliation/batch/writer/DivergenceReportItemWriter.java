package com.kayque.financial_reconciliation.batch.writer;

import com.kayque.financial_reconciliation.domain.dto.ReconciliationResultDTO;
import com.kayque.financial_reconciliation.domain.enums.ReconciliationStatus;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.ItemStreamWriter;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DivergenceReportItemWriter implements ItemStreamWriter<ReconciliationResultDTO> {

    private final FlatFileItemWriter<ReconciliationResultDTO> divergenceFileWriter;

    @Override
    public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        divergenceFileWriter.open(executionContext);
    }

    @Override
    public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        divergenceFileWriter.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        divergenceFileWriter.close();
    }

    @Override
    public void write(Chunk<? extends ReconciliationResultDTO> chunk) throws Exception {
        Chunk<ReconciliationResultDTO> divergencesOnly = new Chunk<>();
        for (ReconciliationResultDTO item : chunk) {
            if (item.getResult() != ReconciliationStatus.RECONCILED) {
                divergencesOnly.add(item);
            }
        }
        if (!divergencesOnly.isEmpty()) {
            divergenceFileWriter.write(divergencesOnly);
        }
    }
}