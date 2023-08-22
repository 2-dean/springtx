package hello.springtx.order;

public class NotEnoughMoneyException extends Exception { //체크예외

    public NotEnoughMoneyException(String message) {
        super(message);
    }
}
