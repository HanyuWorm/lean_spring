package dev.learning.nativepatterns.proxy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TransactionProbeService {
    public boolean callTransactionalMethodThroughSelf() {
        return transactionalProbe();
    }

    @Transactional
    public boolean transactionalProbe() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }
}
