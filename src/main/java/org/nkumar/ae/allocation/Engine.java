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

    public Engine(WarehouseInventoryInfo whInfo, List<StoreModel> storeModels, Statics statics)
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
        if (!storeModel.canBeAllocatedForSkuMatch(sku, statics) || !whInfo.hasAvailableInventory(sku))
        {
            return false;
        }
        allocateSku(sku, storeModel, "SKU Match");
        return true;
    }

    //sku for which sku match did not work
    private boolean allocateExactMatch(String sku, StoreModel storeModel)
    {
        List<String> list = statics.getExactMatchSkus(sku);
        list = storeModel.canBeAllocatedForNonSkuMatch(list, statics);
        Optional<String> maxSku = whInfo.getOneWithMaxStock(list);
        maxSku.ifPresent(allocatedSku -> allocateSku(allocatedSku, storeModel, "Exact Match of " + sku));
        return maxSku.isPresent();
    }

    //sku for which exact sku match did not work
    private boolean allocatePartialMatch(String sku, StoreModel storeModel)
    {
        List<String> list = statics.getPartialMatchSkus(sku);
        list = storeModel.canBeAllocatedForNonSkuMatch(list, statics);
        Optional<String> firstSku = whInfo.getFirstWithAnyStock(list);
        firstSku.ifPresent(allocatedSku -> allocateSku(allocatedSku, storeModel, "Partial Match of " + sku));
        return firstSku.isPresent();
    }

    private void allocateSku(String allocatedSku, StoreModel storeModel, String reason)
    {
        storeModel.allocate(statics.getSkuInfo(allocatedSku), reason);
        whInfo.decrementInventory(allocatedSku);
    }
}
