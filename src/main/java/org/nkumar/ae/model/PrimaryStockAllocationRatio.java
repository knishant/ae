package org.nkumar.ae.model;

import java.util.HashMap;
import java.util.Map;

public final class PrimaryStockAllocationRatio
{
    private final Map<GenderShape, Integer> map = new HashMap<>();

    public void setQuantity(Gender gender, String shape, int quantity)
    {
        map.put(new GenderShape(gender, shape), quantity);
    }

    public void decrementQuantity(Gender gender, String shape, int quantity)
    {
        map.compute(new GenderShape(gender, shape),
                (s, oldQuantity) -> oldQuantity == null ? -quantity : oldQuantity - quantity);
    }

    public int getCapacity()
    {
        //sum of all the values in the map
        return map.values().stream()
                .mapToInt(Integer::intValue).sum();
    }

    public PrimaryStockAllocationRatio()
    {
    }

    public PrimaryStockAllocationRatio(PrimaryStockAllocationRatio ratio)
    {
        //just clone the values
        this.map.putAll(ratio.map);
    }

    public void allocateForEach(Allocator allocator)
    {
        map.forEach((genderShape, count) -> {
            int allocate = allocator.allocate(genderShape.getGender(), genderShape.getShape(), count);
            if (allocate > 0)
            {
                decrementQuantity(genderShape.getGender(), genderShape.getShape(), allocate);
            }
        });
    }
}
