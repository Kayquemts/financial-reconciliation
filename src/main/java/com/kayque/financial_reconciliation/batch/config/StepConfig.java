package com.kayque.financial_reconciliation.batch.config;

import com.kayque.financial_reconciliation.batch.processor.ReconciliationItemProcessor;
import com.kayque.financial_reconciliation.batch.writer.DivergenceReportItemWriter;
import com.kayque.financial_reconciliation.domain.dto.TransactionDTO;
import com.kayque.financial_reconciliation.domain.dto.ReconciliationResultDTO;
import com.kayque.financial_reconciliation.domain.exceptions.InvalidTransactionDataException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.item.ChunkOrientedStep;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class StepConfig {

    @Bean
    public ChunkOrientedStep<TransactionDTO, ReconciliationResultDTO> reconciliationStep(
            @Value("${batch.chunk.size:500}") int chunkSize,
            @Value("${batch.skip.limit:100}") int skipLimit,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<TransactionDTO> transactionItemReader,
            ReconciliationItemProcessor reconciliationItemProcessor,
            ItemWriter<ReconciliationResultDTO> reconciliationCompositeItemWriter,
            DivergenceReportItemWriter divergenceReportItemWriter) {

        return new ChunkOrientedStepBuilder<TransactionDTO, ReconciliationResultDTO>(
                "reconciliationStep", jobRepository, chunkSize)
                .transactionManager(transactionManager)
                .reader(transactionItemReader)
                .processor(reconciliationItemProcessor)
                .writer(reconciliationCompositeItemWriter)
                .stream(divergenceReportItemWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class, NumberFormatException.class, InvalidTransactionDataException.class)
                .skipLimit(skipLimit)
                .build();
    }
}