package org.nkumar.ae;

import org.nkumar.ae.allocation.Engine;
import org.nkumar.ae.allocation.Statics;
import org.nkumar.ae.allocation.StoreModel;
import org.nkumar.ae.input.LoadProcessor;
import org.nkumar.ae.model.PrimaryStockAllocationRatio;
import org.nkumar.ae.model.SKUInfo;
import org.nkumar.ae.model.StoreAllocation;
import org.nkumar.ae.model.StoreInfo;
import org.nkumar.ae.model.StoreInventoryInfo;
import org.nkumar.ae.model.WarehouseInventoryInfo;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Main
{
    public static void main(String[] args)
    {
        int moq = 10;
        File root = new File("tmp/sample");
        System.out.println("root.getAbsolutePath() = " + root.getAbsolutePath());
        //load static info about all the skus
        List<SKUInfo> skuInfoList = LoadProcessor.loadSKU(new File(root, "skuinfo.csv"));

        //load static information about each store.
        List<StoreInfo> storeInfoList = LoadProcessor.loadStoreInfo(new File(root, "storeInfo.csv"));

        Statics statics = new Statics(skuInfoList, storeInfoList);

        //load primary stock allocation ratio for each store
        Map<String/*storeId*/, PrimaryStockAllocationRatio> psarMap = LoadProcessor
                .loadPSAR(new File(root, "psar.csv"), statics.getValidStoreIds());

        //load warehouse inventory details
        WarehouseInventoryInfo whInventory = LoadProcessor
                .loadWarehouseInventoryInfo(new File(root, "warehouseInventory.csv"), statics.getValidSKUs());

        //load inventory details of the store, including what was sold in the previous cycle
        Map<String/*storeId*/, List<StoreInventoryInfo>> storeInventoryInfoMap = LoadProcessor
                .loadStoreInventoryInfo(new File(root, "storeInventory.csv"),
                        statics.getValidStoreIds(), statics.getValidSKUs());

        List<StoreModel> storeModels = storeInfoList.stream()
                .map(storeInfo -> {
                    String storeId = storeInfo.getStoreId();
                    return storeInventoryInfoMap.containsKey(storeId) ? new StoreModel(storeInfo,
                            storeInventoryInfoMap.get(storeId),
                            psarMap.get(storeId),
                            skuInfoList) : null;
                }).filter(Objects::nonNull)
                .filter(s -> s.getTotalGap() >= moq)
                .sorted(Comparator.comparingInt(StoreModel::getSale)
                        .thenComparingInt(StoreModel::getTotalGap)
                        .thenComparingInt(StoreModel::getGrade))
                .collect(Collectors.toList());

        Engine engine = new Engine(whInventory, storeModels, statics);
        List<StoreAllocation> allocate = engine.allocate();
        allocate.forEach(storeAllocation -> {
            System.out.println(storeAllocation);
        });
    }
}
