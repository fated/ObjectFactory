package com.amazon.df.object;

/**
 * Thrown when failed to create object.
 */
public class ObjectCreationException extends RuntimeException {

    /**
     * Serial version id.
     */
    private static final long serialVersionUID = -5839528014823520966L;

    /**
     * Instantiates a new {@link ObjectCreationException}.
     *
     * @param message the error message.
     */
    public ObjectCreationException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new {@link ObjectCreationException}.
     *
     * @param format the error message format.
     * @param args the args of error message
     */
    public ObjectCreationException(final String format, final Object... args) {
        super(String.format(format, args));
    }

    /**
     * Instantiates a new {@link ObjectCreationException}.
     *
     * @param message the error message.
     * @param cause the cause, may be <code>null</code>.
     */
    public ObjectCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ObjectCreationException withCause(final Throwable cause) {
        this.initCause(cause);
        return this;
    }

}
