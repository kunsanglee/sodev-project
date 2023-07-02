package dev.sodev.global.aop.trace.logtrace;


import dev.sodev.global.aop.trace.TraceStatus;

public interface LogTrace {

    TraceStatus begin(String message);

    void end(TraceStatus status);

    void exception(TraceStatus status, Throwable e);
}
