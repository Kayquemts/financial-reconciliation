package com.kayque.financial_reconciliation.reader;

import java.math.BigDecimal;

import com.kayque.financial_reconciliation.domain.dto.TransactionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransactionalItemReaderTest {

    @Autowired
    private FlatFileItemReader<TransactionDTO> reader;

    @Test
    void mustReadFirstLineCorrectly() throws Exception {
        reader.open(new ExecutionContext());
        TransactionDTO transaction = reader.read();

        assertThat(transaction.getTransactionId()).isEqualTo("TRX-010283");
        assertThat(transaction.getAmount()).isEqualTo(new BigDecimal("751.81"));

        reader.close();
    }
}