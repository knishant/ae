package org.nkumar.ae.allocation;

import org.nkumar.ae.model.StoreAllocation;
import org.nkumar.ae.model.WarehouseInventoryInfo;

import java.util.Collection;
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
                .map(StoreModel::getAllocations)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private boolean allocateSKUMatch(String sku, StoreModel storeModel)
    {
        if (!storeModel.canBeAllocatedForSkuMatch(sku, statics) || !whInfo.hasAvailableInventory(sku))
        {
            return false;
        }
        allocateSku(sku, storeModel, "SKU Match", sku);
        return true;
    }

    //sku for which sku match did not work
    private boolean allocateExactMatch(String sku, StoreModel storeModel)
    {
        List<String> list = statics.getExactMatchSkus(sku);
        list = storeModel.canBeAllocatedForNonSkuMatch(list, statics);
        Optional<String> maxSku = whInfo.getOneWithMaxStock(list);
        maxSku.ifPresent(allocatedSku -> allocateSku(allocatedSku, storeModel, "Exact Match", sku));
        return maxSku.isPresent();
    }

    //sku for which exact sku match did not work
    private boolean allocatePartialMatch(String sku, StoreModel storeModel)
    {
        List<String> list = statics.getPartialMatchSkus(sku);
        list = storeModel.canBeAllocatedForNonSkuMatch(list, statics);
        Optional<String> firstSku = whInfo.getFirstWithAnyStock(list);
        firstSku.ifPresent(allocatedSku -> allocateSku(allocatedSku, storeModel, "Partial Match", sku));
        return firstSku.isPresent();
    }

    private void allocateSku(String allocatedSku, StoreModel storeModel, String mode, String secondarySku)
    {
        storeModel.allocate(statics.getSkuInfo(allocatedSku),
                new StoreAllocation(storeModel.getStoreId(), allocatedSku, mode, secondarySku));
        whInfo.decrementInventory(allocatedSku);
    }
}
