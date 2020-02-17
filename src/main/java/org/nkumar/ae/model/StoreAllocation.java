package org.nkumar.ae.model;

import java.util.Collections;
import java.util.Set;

public final class StoreAllocation
{
    private final String storeId;

    private final Set<String/*SKU*/> allocations;

    public StoreAllocation(String storeId, Set<String> allocations)
    {
        this.storeId = storeId;
        this.allocations = Collections.unmodifiableSet(allocations);
    }

    public String getStoreId()
    {
        return storeId;
    }

    public Set<String> getAllocations()
    {
        return allocations;
    }

    @Override
    public String toString()
    {
        return "StoreAllocation{" +
                "storeId='" + storeId + '\'' +
                ", allocations=" + allocations +
                '}';
    }
}
