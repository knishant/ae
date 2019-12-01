package org.nkumar.ae.model;

import java.util.Map;
import java.util.TreeMap;

public final class WarehouseInventoryInfo
{
    private final Map<String/*SKU*/, Integer> map;

    public WarehouseInventoryInfo(Map<String, Integer> input)
    {
        this.map = new TreeMap<>(input);
    }

    public int getAvailableInventory(String sku)
    {
        return map.getOrDefault(sku, 0);
    }

    public void decrementInventory(String sku, int count)
    {
        map.computeIfPresent(sku, (s, oldValue) -> oldValue - count);
    }
}
