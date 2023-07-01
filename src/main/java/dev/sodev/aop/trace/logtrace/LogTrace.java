package dev.sodev.aop.trace.logtrace;


import dev.sodev.aop.trace.TraceStatus;

public interface LogTrace {

    TraceStatus begin(String message);

    void end(TraceStatus status);

    void exception(TraceStatus status, Throwable e);
}
