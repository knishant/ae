package org.nkumar.ae;

import org.nkumar.ae.allocation.Engine;
import org.nkumar.ae.allocation.StoreModel;
import org.nkumar.ae.input.LoadProcessor;
import org.nkumar.ae.model.GenderShapeSKUsMap;
import org.nkumar.ae.model.PrimaryStockAllocationRatio;
import org.nkumar.ae.model.SKUInfo;
import org.nkumar.ae.model.SKUSimilarity;
import org.nkumar.ae.model.StoreAllocation;
import org.nkumar.ae.model.StoreInfo;
import org.nkumar.ae.model.StoreInventoryInfo;
import org.nkumar.ae.model.WarehouseInventoryInfo;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Main
{
    public static void main(String[] args)
    {
        int moq = 10;
        File root = new File("sample");
        List<SKUInfo> skuInfoList = LoadProcessor.loadSKU(new File(root, "skuinfo.csv"));

        SKUSimilarity similarity = SKUSimilarity.buildSKUSimilarity(skuInfoList);

        GenderShapeSKUsMap genderShapeSKUsMap
                = GenderShapeSKUsMap.buildGenderShapeSKUsMap(skuInfoList, similarity);

        Map<String/*storeId*/, PrimaryStockAllocationRatio> psarMap = LoadProcessor
                .loadPSAR(new File(root, "psar.csv"));

        WarehouseInventoryInfo whInventory = LoadProcessor
                .loadWarehouseInventoryInfo(new File(root, "warehouseInventory.csv"));

        Map<String/*storeId*/, List<StoreInventoryInfo>> storeInventoryInfoMap = LoadProcessor
                .loadStoreInventoryInfo(new File(root, "storeInventory.csv"));

        List<StoreInfo> storeInfoList = LoadProcessor.loadStoreInfo(new File(root, "storeInfo.csv"));

        List<StoreModel> storeModels = storeInfoList.stream().map(storeInfo -> {
            String storeId = storeInfo.getStoreId();
            return new StoreModel(storeInfo,
                    storeInventoryInfoMap.get(storeId),
                    psarMap.get(storeId),
                    skuInfoList);
        }).filter(s -> s.getTotalGap() >= moq)
                .sorted(Comparator.comparingInt(StoreModel::getSale)
                        .thenComparingInt(StoreModel::getTotalGap)
                        .thenComparingInt(StoreModel::getGrade).reversed())
                .collect(Collectors.toList());

        Engine engine = new Engine(whInventory, storeModels, genderShapeSKUsMap);
        List<StoreAllocation> allocate = engine.allocate();
        System.out.println("allocate = " + allocate);
    }
}
