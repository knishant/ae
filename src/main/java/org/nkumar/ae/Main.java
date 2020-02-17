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
        File root = new File("sample");
        //load static info about all the skus
        List<SKUInfo> skuInfoList = LoadProcessor.loadSKU(new File(root, "skuinfo.csv"));
        Statics statics = new Statics(skuInfoList);

        //load primary stock allocation ratio for each store
        Map<String/*storeId*/, PrimaryStockAllocationRatio> psarMap = LoadProcessor
                .loadPSAR(new File(root, "psar.csv"));

        //load warehouse inventory details
        WarehouseInventoryInfo whInventory = LoadProcessor
                .loadWarehouseInventoryInfo(new File(root, "warehouseInventory.csv"));

        //load inventory details of the store, including what was sold in the previous cycle
        Map<String/*storeId*/, List<StoreInventoryInfo>> storeInventoryInfoMap = LoadProcessor
                .loadStoreInventoryInfo(new File(root, "storeInventory.csv"));

        //load static information about each store.
        List<StoreInfo> storeInfoList = LoadProcessor.loadStoreInfo(new File(root, "storeInfo.csv"));

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
                        .thenComparingInt(StoreModel::getGrade).reversed())
                .collect(Collectors.toList());

        //TODO reversed in grade above may not be correct

        Engine engine = new Engine(whInventory, storeModels, statics);
        List<StoreAllocation> allocate = engine.allocate();
        System.out.println("allocate = " + allocate);
    }
}
