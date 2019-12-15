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

    public void allocate(String sku)
    {
        allocations.compute(sku, (s, oldValue) -> oldValue == null? 1: oldValue + 1);
    }

    public String getStoreId()
    {
        return storeId;
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
