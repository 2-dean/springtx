package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("트랜잭션 커밋 시작");
        txManager.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }


    @Test
    void double_commit() { //  conn0 1개만 씀 물리 커넥션은  1개만 씀,  커덱션풀에 반납하고 새로쓴것임!
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("트랜잭션1 커밋 시작");
        txManager.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("트랜잭션2 커밋 시작");
        txManager.commit(tx2);
    }

    @Test
    void double_commit_rollback() { //  conn0 1개만 씀 물리 커넥션은  1개만 씀,  커덱션풀에 반납하고 새로쓴것임!
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("트랜잭션1 커밋");
        txManager.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("트랜잭션2 롤백");
        txManager.rollback(tx2);
    }

    @Test
    void inner_commit() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());// 처음 수행된 트랜잭션인가?

        log.info("내부 트랜잭션 시작"); //내부 트랜잭션이 기존에 존재하는 외부 트랜잭션에 참여 Participating in existing transaction
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);    // 이때는 물

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer); // 실제로 이때 커밋이 됨

        // 외부 트랜잭션만 물리 트랜잭션을 시작하고 커밋한다.
        // 처음 트랜잭션을 시작한 외부 트랜잭션이 실제 물리 트랜잭션을 관리하도록 함
        // 물리트랜잭션은 외부 트랜잭션이 끝날때까지 이어져야함
    }


    @Test
    void outer_rollback() {
        log.info("외부 트랜잭션 시작"); //Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("내부 트랜잭션 시작"); // Participating in existing transaction
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);

        log.info("외부 트랜잭션 롤백");
        txManager.rollback(outer); //  Rolling back JDBC transaction on Connection [HikariProxyConnection@2014166743 wrapping conn0: url=jdbc:h2:mem:61cb00dc-c42f-46f7-80a1-b9b178388113 user=SA]
    }

    // 내부롤백
    @Test
    void inner_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition()); // 신규 트랜잭션이 아니기때문에 참여만 가능
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner); // 실제 물리 트랜잭션을 롤백하지 않음 --> 트랜잭션 동기화매니저에 rollback-only=true 마킹됨

        log.info("외부 트랜잭션 커밋");
        Assertions.assertThatThrownBy(() -> txManager.commit(outer)).isInstanceOf(UnexpectedRollbackException.class);
         // 신규 트랜잭션이므로 트랜잭션 동기화 매니저에서  rollback-only=true 인지 확인하고 롤백 -하고
        //UnexpectedRollbackException 런타임 에러를 던짐 -> 기대하지 않은 롤백이 발생했음을 명시
        //Global transaction is marked as rollback-only but transactional code requested commit
        // *** initiating transaction rollback
    }

    @Test
    void inner_rollback_requires_new() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());


        innerLogic();

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);
    }

    private void innerLogic() {
        log.info("내부 트랜잭션 시작");
        // 기존트랜잭션이 있으면 무시하고 새로운 트랜잭션 만듦
        // Suspending current transaction, creating new transaction with name [null]
        // Acquired Connection [HikariProxyConnection@1310414130 wrapping conn1
        // Switching JDBC Connection [HikariProxyConnection@1310414130 wrapping conn1:
        // url=jdbc:h2:mem:894cea57-4a86-48a4-9dca-29a0833e0639 user=SA] to manual commit
        DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
        definition.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW); // option 주기
        TransactionStatus inner = txManager.getTransaction(definition);
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction()); //true
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner);
    }


}
