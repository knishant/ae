package org.nkumar.ae.allocation;

import org.nkumar.ae.model.GenderShape;
import org.nkumar.ae.model.PrimaryStockAllocationRatio;
import org.nkumar.ae.model.SKUInfo;
import org.nkumar.ae.model.StoreInfo;
import org.nkumar.ae.model.StoreInventoryInfo;

import java.util.HashSet;
import java.util.LinkedHashMap;
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

    private final int sale;
    private final int totalGap;

    private final Set<String> skusToAllocate = new HashSet<>();
    private final Set<String> skusInStore = new HashSet<>();
    private final Map<String, String> skusAllocated = new LinkedHashMap<>();

    private final PrimaryStockAllocationRatio ratioGap;

    public StoreModel(StoreInfo storeInfo, List<StoreInventoryInfo> inventoryInfoList,
            PrimaryStockAllocationRatio ratio, List<SKUInfo> skuInfoList)
    {
        this.storeId = storeInfo.getStoreId();
        this.name = storeInfo.getName();
        this.grade = storeInfo.getGrade();
        this.sale = inventoryInfoList.stream().mapToInt(StoreInventoryInfo::getSold).sum();
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
            if (info.getSold() > 0 && info.getAge() < MAX_SHELF_AGE)
            {
                skusToAllocate.add(skuInfo.getSKU());
            }
        });
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

    public int getSale()
    {
        return sale;
    }

    public int getTotalGap()
    {
        return totalGap;
    }

    public Set<String> getSkusToAllocate()
    {
        return skusToAllocate;
    }

    Map<String, String> getSkusAllocated()
    {
        return skusAllocated;
    }

    private PrimaryStockAllocationRatio getRatioGap()
    {
        return ratioGap;
    }

    void allocate(SKUInfo skuInfo, String reason)
    {
        skusAllocated.put(skuInfo.getSKU(), reason);
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
                        !skusAllocated.containsKey(sku)
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
