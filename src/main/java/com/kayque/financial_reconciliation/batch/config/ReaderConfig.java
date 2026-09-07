package com.kayque.financial_reconciliation.batch.config;

import com.kayque.financial_reconciliation.domain.dto.TransactionDTO;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ReaderConfig {

    @Bean
    public FlatFileItemReader<TransactionDTO> transactionItemReader(@Value("${batch.input.file}") Resource resource){
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("transactionId","transactionDate","amount","gatewayStatus");

        DefaultLineMapper<TransactionDTO> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
            setTargetType(TransactionDTO.class);
        }});

        return new FlatFileItemReaderBuilder<TransactionDTO>()
                .name("transactionalItemReader")
                .resource(resource)
                .linesToSkip(1)
                .lineMapper(lineMapper)
                .strict(true)
                .build();
    }
}
