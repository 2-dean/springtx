package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class TxLevelTest {

    @Autowired LevelService service;

    @Test
    void OrderTest(){
        service.write();
        service.read();
    }

    @TestConfiguration //쩝ㅋ
    static class TxLevelTestConfig {
        @Bean
        LevelService levelService() {
            return new LevelService();
        }
    }


    @Slf4j
    @Transactional(readOnly = true) // 읽기전용트랜잭션
    static class LevelService {

        @Transactional(readOnly = false) //(readOnly = false) 기본옵션임
        public void write() {
            log.info("call write");
            printTxInfo();
        }

        public void read() { //readonly = true로 적용됨
            log.info("call read");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("tx readOnly={}", readOnly);
        }

    }

}
