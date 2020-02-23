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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Main
{
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args)
    {
        int moq = 10;
        File root = new File("tmp/sample");
        LOG.log(Level.INFO, "Loading files in {0}", root.getAbsolutePath());
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

        LOG.log(Level.INFO, "Number of stores = {0}", storeInfoList.size());

        List<StoreModel> storeModels = storeInfoList.stream()
                .filter(storeInfo -> {
                    String storeId = storeInfo.getStoreId();
                    boolean valid = storeInventoryInfoMap.containsKey(storeId)
                            && psarMap.containsKey(storeId);
                    if (!valid)
                    {
                        LOG.log(Level.WARNING,
                                "Not allocating store {0} as either inventory info or PSAR is missing", storeId);
                    }
                    return valid;
                })
                .map(storeInfo -> {
                    String storeId = storeInfo.getStoreId();
                    return new StoreModel(storeInfo,
                            storeInventoryInfoMap.get(storeId),
                            psarMap.get(storeId),
                            skuInfoList);
                })
                .filter(s -> s.getTotalGap() >= moq)
                .sorted(Comparator.comparingInt(StoreModel::getSale)
                        .thenComparing(StoreModel::getTotalGap)
                        .thenComparingInt(StoreModel::getGrade).reversed())
                .collect(Collectors.toList());

        LOG.log(Level.INFO, "Allocating SKUs to {0} stores, whose gap is more than moq",
                storeModels.size());

//        storeModels.forEach(s -> {
//            System.out.println(s.getStoreId() + " -> sale=" + s.getSale() + ", gap=" + s.getTotalGap() + ", grade=" + s.getGrade());
//        });

        Engine engine = new Engine(whInventory, storeModels, statics);
        List<StoreAllocation> allocate = engine.allocate();
        allocate.forEach(storeAllocation -> {
            System.out.println(storeAllocation);
        });

        System.out.println("Allocated " + (whInventory.getInitialTotalInventory() - whInventory.getTotalInventory())
                + " from " + whInventory.getInitialTotalInventory() + " items");
    }
}
