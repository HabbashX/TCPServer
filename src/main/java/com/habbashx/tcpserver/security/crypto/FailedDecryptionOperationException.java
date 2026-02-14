package com.habbashx.tcpserver.security.crypto;

import java.io.Serial;

public class FailedDecryptionOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 14343583524243L;

    public FailedDecryptionOperationException(String message) {
        super(message);
    }

}
