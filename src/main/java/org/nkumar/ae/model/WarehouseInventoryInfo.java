package org.nkumar.ae.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

public final class WarehouseInventoryInfo
{
    private final Map<String/*SKU*/, Integer> map;

    private final int initialTotalInventory;

    public WarehouseInventoryInfo(Map<String, Integer> input)
    {
        this.map = new TreeMap<>(input);
        this.initialTotalInventory = getTotalInventory();
    }

    public Set<String> getSkus()
    {
        return Collections.unmodifiableSet(map.keySet());
    }

    public int getInitialTotalInventory()
    {
        return initialTotalInventory;
    }

    public int getTotalInventory()
    {
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean hasAvailableInventory(String sku)
    {
        return map.getOrDefault(sku, 0) > 0;
    }

    public void decrementInventory(String sku)
    {
        map.computeIfPresent(sku, (s, oldValue) -> oldValue - 1);
    }

    //returns sku from the passed list of skus, which have max inventory available
    //returns nothing if none have any inventory available
    public Optional<String> getOneWithMaxStock(List<String> skus)
    {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .filter(entry -> skus.contains(entry.getKey()))
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey);
    }

    //returns first sku from the passed list of skus, which have any inventory available
    //returns nothing if none have any inventory available
    public Optional<String> getFirstWithAnyStock(List<String> skus)
    {
        return skus.stream().filter(this::hasAvailableInventory).findAny();
    }
}
