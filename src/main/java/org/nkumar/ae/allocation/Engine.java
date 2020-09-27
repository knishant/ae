package org.nkumar.ae.allocation;

import org.nkumar.ae.model.StoreAllocation;
import org.nkumar.ae.model.WarehouseInventoryInfo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Engine
{
    private static final Logger LOG = Logger.getLogger(Engine.class.getName());

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
        int allocations;
        int iteration = 0;
        do
        {
            allocations = 0;
            iteration++;
            for (StoreModel storeModel : storeModels)
            {
                allocations += storeModel.applyAllocationRule(
                        skuToAllocate -> allocateSKUMatch(skuToAllocate, storeModel));
            }
            LOG.log(Level.INFO, "Allocations in SKU match iteration {0} = {1}", new Object[]{iteration, allocations});
        } while (allocations > 0);
        {
            allocations = 0;
            for (StoreModel storeModel : storeModels)
            {
                allocations += storeModel.applyAllocationRule(
                        skuToAllocate -> allocateExactMatch(skuToAllocate, storeModel));
            }
            LOG.log(Level.INFO, "Allocations in exact match = {0}", allocations);
        }
        {
            allocations = 0;
            for (StoreModel storeModel : storeModels)
            {
                allocations += storeModel.applyAllocationRule(
                        skuToAllocate -> allocatePartialMatch(skuToAllocate, storeModel));
            }
            LOG.log(Level.INFO, "Allocations in partial match = {0}", allocations);
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
        storeModel.allocateItem(statics.getSkuInfo(allocatedSku),
                new StoreAllocation(storeModel.getStoreId(), allocatedSku, mode, secondarySku));
        whInfo.decrementInventory(allocatedSku);
    }
}
