package com.sunjinsong.exceptions;

public class NetworkException {
    public static class ConnectException extends RuntimeException {
        public ConnectException(String message)
        {
            super(message);
        }
    }
}
