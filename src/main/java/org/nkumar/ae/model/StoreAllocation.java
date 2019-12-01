package org.nkumar.ae.model;

import java.util.Map;
import java.util.TreeMap;

public final class StoreAllocation
{

    private final String storeId;

    private final Map<String/*SKU*/, Integer> allocations = new TreeMap<>();

    public StoreAllocation(String storeId)
    {
        this.storeId = storeId;
    }
}
