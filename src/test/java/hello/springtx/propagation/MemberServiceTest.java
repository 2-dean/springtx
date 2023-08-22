package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    /**
     * 요구사항 : 회원 가입을 시도한 로그를 남기는데 실패하더라도 회원 가입은 유지되어야 한다
     */

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * memberService        @Transactional : OFF
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON
     */
    @Test
    void outerTxoff_success() {
        //given
        String username = "outerTxoff_success";

        //when
        memberService.joinV1(username);

        //when : 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService        @Transactional : ON
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON Exception
     */
    @Test
    void outerTxOm_fail() {
        //given
        String username = "로그예외_outerTxOm_fail";

        //when
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
                                                            .isInstanceOf(RuntimeException.class);

        //when :  멤버는 저장, 로그는 롤백
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }


    /**
     * memberService        @Transactional : ON
     * memberRepository     @Transactional : OFF
     * logRepository        @Transactional : OFF
     */
    @Test
    void singleTx() {
        //given
        String username = "outerTxoff_success";

        //when
        memberService.joinV1(username);

        //when : 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }



    /**
     * memberService        @Transactional : ON
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON
     */
    @Test
    void outerTxOn_success() {
        //given
        String username = "outerTxOn_success";

        //when
        memberService.joinV1(username);

        //when : 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService        @Transactional : ON
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON Exception
     */
    @Test
    void outerTxOn_fail() {
        //given
        String username = "로그예외_outerTxOn_fail";

        //when
        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        //when : 모든 데이터가 롤백된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }


    /**
     * memberService        @Transactional : ON
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON Exception
     */
    @Test
    void recoverException_fail() {
        //given
        String username = "로그예외_recoverException_fail";

        //when
        assertThatThrownBy(() -> memberService.joinV2(username)).isInstanceOf(UnexpectedRollbackException.class);

        //기대 :  회원은 저장, LOG는 저장되지않음
        //when 실제 -> : 모든 데이터가 롤백된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService        @Transactional : ON
     * memberRepository     @Transactional : ON
     * logRepository        @Transactional : ON(REQUIRES_NEW) Exception -> 물리 트랜잭션 분리
     */
    @Test
    void recoverException_success() {
        //given
        String username = "로그예외_recoverException_success";

        //when
        memberService.joinV2(username);

        //결과 : 회원은 저장, LOG 는 롤백
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }


}