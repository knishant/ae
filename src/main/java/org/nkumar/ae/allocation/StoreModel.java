package org.nkumar.ae.allocation;

import org.nkumar.ae.model.PrimaryStockAllocationRatio;
import org.nkumar.ae.model.SKUInfo;
import org.nkumar.ae.model.StoreInfo;
import org.nkumar.ae.model.StoreInventoryInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class StoreModel
{
    private final String storeId;
    private final String name;
    private final int grade;

    private final int sale;
    private final int totalGap;

    private final PrimaryStockAllocationRatio ratioGap;
    private final Map<String, SKUInfo> skuInfoMap;

    public StoreModel(StoreInfo storeInfo, List<StoreInventoryInfo> inventoryInfoList,
            PrimaryStockAllocationRatio ratio, List<SKUInfo> skuInfoList)
    {
        this.storeId = storeInfo.getStoreId();
        this.name = storeInfo.getName();
        this.grade = storeInfo.getGrade();
        this.sale = inventoryInfoList.stream().mapToInt(StoreInventoryInfo::getSold).sum();
        int totalAvailable = inventoryInfoList.stream().mapToInt(StoreInventoryInfo::getAvailable).sum();
        this.totalGap = ratio.getCapacity() - totalAvailable;

        this.skuInfoMap = skuInfoList.stream()
                .collect(Collectors.toMap(SKUInfo::getSKU, Function.identity()));

        //clone the PSAR and then deduct what is available
        //what remains is the gap which has to be allocated
        this.ratioGap = new PrimaryStockAllocationRatio(ratio);
        inventoryInfoList.forEach(info -> {
            SKUInfo skuInfo = this.skuInfoMap.get(info.getSKU());
            Objects.requireNonNull(skuInfo, "SKU is not valid : " + info.getSKU());
            this.ratioGap.decrementQuantity(skuInfo.getGender(), skuInfo.getShape(), info.getAvailable());
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
}
