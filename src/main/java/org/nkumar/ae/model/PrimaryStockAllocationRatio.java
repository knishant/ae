package org.nkumar.ae.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

public final class PrimaryStockAllocationRatio
{
    private final Map<Gender, Map<String, Integer>> map = new EnumMap<>(Gender.class);

    public void setQuantity(Gender gender, String shape, int quantity)
    {
        map.computeIfAbsent(gender, g -> new TreeMap<>()).put(shape, quantity);
    }

    public void decrementQuantity(Gender gender, String shape, int quantity)
    {
        map.computeIfAbsent(gender, g -> new TreeMap<>())
                .compute(shape, (s, oldQuantity) -> oldQuantity == null ? -quantity : oldQuantity - quantity);
    }

    public int getCapacity()
    {
        //sum of all the values in the map
        return map.values().stream()
                .flatMap(shapeMap -> shapeMap.values().stream())
                .mapToInt(Integer::intValue).sum();
    }

    public PrimaryStockAllocationRatio()
    {
    }

    public PrimaryStockAllocationRatio(PrimaryStockAllocationRatio ratio)
    {
        //just clone the values
        ratio.map.forEach((gender, shapeMap) -> this.map.put(gender, new TreeMap<>(shapeMap)));
    }

    public void allocateForEach(Allocator allocator)
    {
        map.forEach((gender, shapeMap) ->
                shapeMap.forEach((shape, count) ->
                {
                    int allocate = allocator.allocate(gender, shape, count);
                    if (allocate > 0)
                    {
                        decrementQuantity(gender, shape, allocate);
                    }
                }));
    }
}
