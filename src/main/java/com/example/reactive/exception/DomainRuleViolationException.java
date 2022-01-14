package com.example.reactive.exception;

public class DomainRuleViolationException extends RuntimeException {

    private String errCode;

    public DomainRuleViolationException() {
    }

    public DomainRuleViolationException(String message) {
        super(message);
    }

    public DomainRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DomainRuleViolationException(Throwable cause) {
        super(cause);
    }

    public DomainRuleViolationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DomainRuleViolationException(String message, String errCode) {
        super(message);
        this.errCode = errCode;
    }

    public DomainRuleViolationException(String message, Throwable cause, String errCode) {
        super(message, cause);
        this.errCode = errCode;
    }

    public DomainRuleViolationException(Throwable cause, String errCode) {
        super(cause);
        this.errCode = errCode;
    }

    public DomainRuleViolationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String errCode) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errCode = errCode;
    }

    public String getErrCode() {
        return errCode;
    }
}
