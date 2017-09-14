package org.jcp.pc.base.exception;

/**
 * Thrown when an unexpected concurrent call occureres. This can happen for example due to incorrect producer scheduling
 * configuration.
 */
public class UnexpectedCallException extends IllegalAccessError {
    public UnexpectedCallException(final String s) {
        super(s);
    }
}
