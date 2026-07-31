package com.endeleya.ia;

@FunctionalInterface
public interface InvoiceAgentRunner {
    String run(InvoiceDraft draft);
}
