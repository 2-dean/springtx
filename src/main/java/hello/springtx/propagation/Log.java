package hello.springtx.propagation;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Log {
    // db에 남기는 log

    @Id
    @GeneratedValue
    private Long id;
    private String message;

    public Log (String message) {
        this.message = message;
    }

    public Log() {

    }
}
