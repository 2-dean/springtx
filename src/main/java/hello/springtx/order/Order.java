package hello.springtx.order;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.processing.Generated;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity //jpa
@Table(name = "orders") // orders 라는 DB테이블과 매핑
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private String username; //정상, 예외, 잔고부족
    private String payStatus; //대기, 완료
}
