package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;

@SpringBootTest
public class InitTxTest {

    @Autowired Hello hello;

    @Test
    void go() {
        //초기화 코드는 스프링이 초기화 시점에 호출한다.
    }

    @TestConfiguration
    static class InitTxTestConfig {
        @Bean
        Hello hello() {
            return new Hello();
        }
    }

    @Slf4j
    static class Hello {

        @PostConstruct // 의존석 주입이 끝난 뒤 실행될 메소드에 적용
        @Transactional
        public void initV1() {

            // Hello Init @PostConstruct tx active=false
            // 초기화 코드 먼저 호출된 후 트랜잭션 AOP 적용됨 초기화 시점에는 해당메서드에서 트랜잭션을 획득할 수 없음
            boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello Init @PostConstruct tx active={}",isActive);
        }

        @EventListener(ApplicationReadyEvent.class) // 스프링 컨테이너가 완성된 후 해당 메소드 호출
        @Transactional
        public void initV2() {
            boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello Init ApplicationReadyEvent.class tx active={}",isActive);
        }

    }


}
