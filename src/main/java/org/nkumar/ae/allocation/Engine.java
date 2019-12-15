package org.nkumar.ae.allocation;

import org.nkumar.ae.model.Gender;
import org.nkumar.ae.model.GenderShapeSKUsMap;
import org.nkumar.ae.model.PrimaryStockAllocationRatio;
import org.nkumar.ae.model.StoreAllocation;
import org.nkumar.ae.model.WarehouseInventoryInfo;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Engine
{
    private final WarehouseInventoryInfo whInfo;
    private final List<StoreModel> storeModels;
    private final GenderShapeSKUsMap genderShapeSKUsMap;

    public Engine(WarehouseInventoryInfo whInfo, List<StoreModel> storeModels,
             GenderShapeSKUsMap genderShapeSKUsMap)
    {
        this.whInfo = whInfo;
        this.storeModels = storeModels;
        this.genderShapeSKUsMap = genderShapeSKUsMap;
    }

    public List<StoreAllocation> allocate()
    {
        List<StoreAllocation> list = storeModels.stream().map(m -> new StoreAllocation(m.getStoreId()))
                .collect(Collectors.toList());
        Map<String, StoreAllocation> allocations = list.stream()
                .collect(Collectors.toMap(StoreAllocation::getStoreId, Function.identity()));
        for (StoreModel storeModel : storeModels)
        {
            PrimaryStockAllocationRatio gap = storeModel.getRatioGap();
            StoreAllocation storeAllocation = allocations.get(storeModel.getStoreId());
            gap.allocateForEach((gender, shape, count) ->
                    allocateFor(gender, shape, count, true, storeAllocation));
            gap.allocateForEach((gender, shape, count) ->
                    allocateFor(gender, shape, count, false, storeAllocation));
        }
        return list;
    }

    private int allocateFor(Gender gender, String shape, int count, boolean exact,
            StoreAllocation storeAllocation)
    {
        if (count <= 0)
            return 0;
        List<String> skUs = genderShapeSKUsMap.getSKUs(gender, shape, exact).stream()
                //filer out skus for which there is no inventory
                .filter(sku -> whInfo.getAvailableInventory(sku) > 0)
                .collect(Collectors.toList());
        //allocating each sku only once
        int allocationCount = Math.min(count, skUs.size());
        for (int i = 0; i < allocationCount; i++)
        {
            String allocatedSKU = skUs.get(i);
            storeAllocation.allocate(allocatedSKU);
            whInfo.decrementInventory(allocatedSKU, 1);
        }
        return allocationCount;
    }
}
