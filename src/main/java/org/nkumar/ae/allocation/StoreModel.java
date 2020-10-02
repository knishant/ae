package org.nkumar.ae.allocation;

import org.nkumar.ae.model.GenderShape;
import org.nkumar.ae.model.PrimaryStockAllocationRatio;
import org.nkumar.ae.model.SKUInfo;
import org.nkumar.ae.model.StoreAllocation;
import org.nkumar.ae.model.StoreInfo;
import org.nkumar.ae.model.StoreInventoryInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class StoreModel
{
    //do not replace skus which have been on shelf for more than this limit
    private static final int MAX_SHELF_AGE = 120;

    private final String storeId;
    private final String name;
    private final int grade;

    private final int toReplenishCount;
    private final int totalGap;

    //sku and the count for the sku to replenish
    private final Map<String, Integer> skusToAllocate = new HashMap<>();

    //sku already in store
    private final Set<String> skusInStore = new HashSet<>();

    //skus which were allocated during the allocation cycle
    private final Set<String> skusAllocated = new HashSet<>();

    //full list of allocations
    private final List<StoreAllocation> allocations = new ArrayList<>();

    private final PrimaryStockAllocationRatio ratioGap;

    public StoreModel(StoreInfo storeInfo, List<StoreInventoryInfo> inventoryInfoList,
            PrimaryStockAllocationRatio ratio, List<SKUInfo> skuInfoList)
    {
        this.storeId = storeInfo.getStoreId();
        this.name = storeInfo.getName();
        this.grade = storeInfo.getGrade();
        int totalAvailable = inventoryInfoList.stream().mapToInt(StoreInventoryInfo::getAvailable).sum();
        this.totalGap = ratio.getCapacity() - totalAvailable;

        Map<String/*SKU*/, SKUInfo> skuInfoMap = skuInfoList.stream()
                .collect(Collectors.toMap(SKUInfo::getSKU, Function.identity()));

        //clone the PSAR and then deduct what is available
        //what remains is the gap which has to be allocated
        this.ratioGap = new PrimaryStockAllocationRatio(ratio);
        inventoryInfoList.forEach(info -> {
            SKUInfo skuInfo = skuInfoMap.get(info.getSKU());
            Objects.requireNonNull(skuInfo, "SKU is not valid : " + info.getSKU());
            if (info.getAvailable() > 0)
            {
                this.ratioGap.decrementQuantity(skuInfo.getGender(), skuInfo.getShape(), info.getAvailable());
                skusInStore.add(info.getSKU());
            }
            //TODO we still do not prevent aged skus from being recommended by non-sku match
            if (info.getSold() > 0)
                    //&& info.getAge() < MAX_SHELF_AGE)
            {
                skusToAllocate.put(skuInfo.getSKU(), info.getSold());
            }
        });
        this.toReplenishCount = this.skusToAllocate.values().stream().mapToInt(Integer::intValue).sum();
    }

    public String getStoreId()
    {
        return storeId;
    }

    public String getName()
    {
        return name;
    }

    public int getGrade()
    {
        return grade;
    }

    public int getToReplenishCount()
    {
        return toReplenishCount;
    }

    public int getTotalGap()
    {
        return totalGap;
    }

    int applyAllocationRule(Function<String, Boolean> allocationFunction)
    {
        return (int) new HashSet<>(skusToAllocate.keySet()).stream()
                .map(allocationFunction)
                .filter(status -> status)
                .count();
    }

    List<StoreAllocation> getAllocations()
    {
        return allocations;
    }

    private PrimaryStockAllocationRatio getRatioGap()
    {
        return ratioGap;
    }

    void allocateItem(SKUInfo skuInfo, StoreAllocation allocation)
    {
        allocations.add(allocation);
        skusAllocated.add(skuInfo.getSKU());
        skusToAllocate.computeIfPresent(allocation.getReplenishmentFor(),
                (key, count) -> count <= 1 ? null : count - 1);
        ratioGap.decrementQuantity(skuInfo.getGender(), skuInfo.getShape(), 1);
    }

    //return the filtered list of skus which can be allocated
    //that is not there in the store
    //its gendershape allocation gap is positive
    List<String> canBeAllocatedForNonSkuMatch(List<String> skuSet, Statics statics)
    {
        return skuSet.stream()
                .filter(sku ->
                        //sku was not allocated by any previous rule
                        !skusAllocated.contains(sku)
                                //sku was not there already in the store even before any allocation
                                && !skusInStore.contains(sku)
                                //psar for this sku still has a gap
                                && psarCheck(sku, statics))
                .collect(Collectors.toList());
    }

    boolean canBeAllocatedForSkuMatch(String sku, Statics statics)
    {
        //for sku match, same sku can be allocated multiple times as long as psar gap is met
        return psarCheck(sku, statics);
    }

    //check that psar for this sku still has a gap
    private boolean psarCheck(String sku, Statics statics)
    {
        GenderShape gs = statics.getGenderShapeForSKU(sku);
        return this.getRatioGap().needsAllocation(gs);
    }
}
