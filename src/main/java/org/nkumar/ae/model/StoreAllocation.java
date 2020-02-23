package org.nkumar.ae.model;

import java.util.Collections;
import java.util.Map;

public final class StoreAllocation
{
    private final String storeId;

    private final Map<String/*SKU*/, String/*reason*/> allocations;

    public StoreAllocation(String storeId, Map<String, String> allocations)
    {
        this.storeId = storeId;
        this.allocations = Collections.unmodifiableMap(allocations);
    }

    public String getStoreId()
    {
        return storeId;
    }

    public Map<String, String> getAllocations()
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
