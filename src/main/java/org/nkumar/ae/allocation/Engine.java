package org.nkumar.ae.allocation;

import org.nkumar.ae.model.StoreAllocation;
import org.nkumar.ae.model.WarehouseInventoryInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Engine
{
    private final WarehouseInventoryInfo whInfo;
    private final List<StoreModel> storeModels;
    private final Statics statics;

    public Engine(WarehouseInventoryInfo whInfo, List<StoreModel> storeModels,
            Statics statics)
    {
        this.whInfo = whInfo;
        this.storeModels = storeModels;
        this.statics = statics;
    }

    public List<StoreAllocation> allocate()
    {
        for (StoreModel storeModel : storeModels)
        {
            storeModel.getSkusToAllocate().removeIf(skuToAllocate -> allocateSKUMatch(skuToAllocate, storeModel));
        }
        for (StoreModel storeModel : storeModels)
        {
            storeModel.getSkusToAllocate().removeIf(skuToAllocate -> allocateExactMatch(skuToAllocate, storeModel));
        }
        for (StoreModel storeModel : storeModels)
        {
            storeModel.getSkusToAllocate().removeIf(skuToAllocate -> allocatePartialMatch(skuToAllocate, storeModel));
        }
        return storeModels.stream()
                .map(model -> new StoreAllocation(model.getStoreId(), model.getSkusAllocated()))
                .collect(Collectors.toList());
    }

    private boolean allocateSKUMatch(String sku, StoreModel storeModel)
    {
        if (!storeModel.canBeAllocated(sku, statics) || !whInfo.hasAvailableInventory(sku))
        {
            return false;
        }
        storeModel.allocate(statics.getSkuInfo(sku), "SKU Match");
        whInfo.decrementInventory(sku);
        return true;
    }

    //sku for which sku match did not work
    private boolean allocateExactMatch(String sku, StoreModel storeModel)
    {
        List<String> list = statics.getExactMatchSkus(sku);
        list = storeModel.canBeAllocated(list, statics);
        Optional<String> maxSku = whInfo.getOneWithMaxStock(list);
        return allocateSku(maxSku, storeModel, "Exact Match of " + sku);
    }

    private boolean allocateSku(Optional<String> sku, StoreModel storeModel, String reason)
    {
        sku.ifPresent(allocatedSku -> {
            storeModel.allocate(statics.getSkuInfo(allocatedSku), reason);
            whInfo.decrementInventory(allocatedSku);
        });
        return sku.isPresent();
    }

    //sku for which exact sku match did not work
    private boolean allocatePartialMatch(String sku, StoreModel storeModel)
    {
        List<String> list = statics.getPartialMatchSkus(sku);
        list = storeModel.canBeAllocated(list, statics);
        Optional<String> firstSku = whInfo.getFirstWithAnyStock(list);
        return allocateSku(firstSku, storeModel, "Partial Match of " + sku);
    }
}
