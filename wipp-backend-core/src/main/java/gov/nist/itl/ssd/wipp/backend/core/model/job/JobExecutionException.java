/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.backend.core.model.job;

/**
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 *
 */
public class JobExecutionException extends Exception {

    /**
     * Constructs an instance of <code>JobExecutionException</code> with the
     * specified detail message.
     *
     * @param message the detail message.
     */
    public JobExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i>
     * automatically incorporated in this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by
     * the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method). (A <tt>null</tt> value is permitted, and
     * indicates that the cause is nonexistent or unknown.)
     */
    public JobExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

