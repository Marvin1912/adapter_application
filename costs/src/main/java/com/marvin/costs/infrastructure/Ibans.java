package com.marvin.costs.infrastructure;

import java.util.Set;

/** Provides a set of IBANs used for filtering cost import transactions. */
public interface Ibans {

    /**
     * Returns the set of IBANs.
     *
     * @return the set of IBANs
     */
    Set<String> getIbans();
}
