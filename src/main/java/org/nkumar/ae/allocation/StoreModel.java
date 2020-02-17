package org.nkumar.ae.allocation;

import org.nkumar.ae.model.GenderShape;
import org.nkumar.ae.model.PrimaryStockAllocationRatio;
import org.nkumar.ae.model.SKUInfo;
import org.nkumar.ae.model.StoreInfo;
import org.nkumar.ae.model.StoreInventoryInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class StoreModel
{
    private final String storeId;
    private final String name;
    private final int grade;

    private final int sale;
    private final int totalGap;

    private final Set<String> skusToAllocate = new HashSet<>();
    private final Set<String> skusInStore = new HashSet<>();
    private final Set<String> skusAllocated = new HashSet<>();

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
            if (info.getSold() > 0 && info.getAge() < 4)
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

    public Set<String> getSkusAllocated()
    {
        return skusAllocated;
    }

    public PrimaryStockAllocationRatio getRatioGap()
    {
        return ratioGap;
    }

    public void allocate(SKUInfo skuInfo)
    {
        skusAllocated.add(skuInfo.getSKU());
        ratioGap.decrementQuantity(skuInfo.getGender(), skuInfo.getShape(), 1);
    }

    //return the filtered list of skus which can be allocated
    //that is not there in the store
    //its gendershape allocation gap is positive
    public List<String> canBeAllocated(List<String> skuSet, Statics statics)
    {
        return skuSet.stream()
                .filter(sku -> canBeAllocated(sku,statics))
                .collect(Collectors.toList());
    }

    public boolean canBeAllocated(String sku, Statics statics)
    {
        GenderShape gs = statics.getGenderShapeForSKU(sku);
        return !skusAllocated.contains(sku) && !skusInStore.contains(sku) && this.getRatioGap().needsAllocation(gs);
    }
}
