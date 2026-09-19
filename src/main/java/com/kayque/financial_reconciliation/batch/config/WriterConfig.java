package com.kayque.financial_reconciliation.batch.config;

import com.kayque.financial_reconciliation.batch.writer.DivergenceReportItemWriter;
import com.kayque.financial_reconciliation.batch.writer.OrderReconciliationItemWriter;
import com.kayque.financial_reconciliation.domain.dto.ReconciliationResultDTO;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

@Configuration
public class WriterConfig {

    @Bean
    public FlatFileItemWriter<ReconciliationResultDTO> divergenceFileWriter(
            @Value("${batch.output.file:file:./output/divergence-report.csv}") String outputPath) {

        return new FlatFileItemWriterBuilder<ReconciliationResultDTO>()
                .name("divergenceFileWriter")
                .resource(new FileSystemResource(outputPath.replaceFirst("^file:", "")))
                .headerCallback(writer -> writer.write(
                        "transactionId,databaseAmount,gatewayAmount,databaseStatus,gatewayStatus,result,observation,processingDate"))
                .lineAggregator(item -> String.join(",",
                        item.getTransactionId(),
                        String.valueOf(item.getDatabaseAmount()),
                        String.valueOf(item.getGatewayAmount()),
                        String.valueOf(item.getDatabaseStatus()),
                        String.valueOf(item.getGatewayStatus()),
                        item.getResult().name(),
                        item.getObservation(),
                        item.getProcessingDate().toString()))
                .build();
    }

    @Bean
    public CompositeItemWriter<ReconciliationResultDTO> reconciliationCompositeItemWriter(
            OrderReconciliationItemWriter orderReconciliationItemWriter,
            DivergenceReportItemWriter divergenceReportItemWriter) {

        return new CompositeItemWriter<>(
                List.<ItemWriter<? super ReconciliationResultDTO>>of(
                        orderReconciliationItemWriter,
                        divergenceReportItemWriter
                )
        );
    }
}