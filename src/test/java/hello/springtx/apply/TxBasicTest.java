package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class TxBasicTest {

    @Autowired BasicService basicService;

    @Test
    void proxyCheck() {
        log.info("aop class={}", basicService.getClass());
        assertThat(AopUtils.isAopProxy(basicService)).isTrue(); // 현재 쓰레드에 트랜잭션이 적용되어있는지 확인
    }

    @Test
    void txTest() {
        basicService.tx();
        basicService.nonTx();
    }


    @TestConfiguration
    static class TxApplyBasicConfig {
        @Bean
        BasicService basicService() {
            return new BasicService();
        }
    }

    @Slf4j
    static class BasicService {
        @Transactional // 트랜잭션 적용대상
        public void tx() {
            log.info("call Tx");
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive(); // 트랜잭션이 적용됐는가?
            log.info("tx active = {}", txActive);
        }

        public void nonTx(){
            log.info("call nonTx");
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive(); // 트랜잭션이 적용됐는가?
            log.info("tx active = {}", txActive);
        }
    }


}
