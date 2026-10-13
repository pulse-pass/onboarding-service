package com.pulsepass.onboardingservice.domain;

public interface UnitOfWork {

    void begin();

    void commit();

    void rollback();

    boolean isActive();
}
