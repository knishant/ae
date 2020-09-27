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
import org.nkumar.ae.output.StoreProcessor;

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
        //moq - minimum order quantity
        //if the gap in a store is less than this value, then the store is ignored for allocation
        int moq = 10;

        //root directory where the csvs are located
        File root = new File("tmp/sample");
        LOG.log(Level.INFO, "Loading files from {0}", root.getAbsolutePath());

        //load static info about all the skus
        //duplicate skus are ignored
        List<SKUInfo> skuInfoList = LoadProcessor.loadSKU(new File(root, "skuinfo.csv"));
        LOG.log(Level.INFO, "Loaded {0} valid skus from skuinfo.csv", skuInfoList.size());

        //load static information about each store.
        List<StoreInfo> storeInfoList = LoadProcessor.loadStoreInfo(new File(root, "storeInfo.csv"));
        LOG.log(Level.INFO, "Loaded {0} valid store infos from storeInfo.csv", storeInfoList.size());

        //combine all the static information, like sku list, store list, sku similarity etc
        Statics statics = new Statics(skuInfoList, storeInfoList);

        //load primary stock allocation ratio for each valid store
        Map<String/*storeId*/, PrimaryStockAllocationRatio> psarMap = LoadProcessor
                .loadPSAR(new File(root, "psar.csv"), statics.getValidStoreIds());
        LOG.log(Level.INFO, "Loaded PSAR for {0} stores from psar.csv", psarMap.size());

        //load warehouse inventory details for each valid sku
        WarehouseInventoryInfo whInventory = LoadProcessor
                .loadWarehouseInventoryInfo(new File(root, "warehouseInventory.csv"), statics.getValidSKUs());
        LOG.log(Level.INFO, "Loaded warehouse inventory numbers for {0} skus from warehouseInventory.csv",
                whInventory.getNumOfSkus());
        LOG.log(Level.WARNING, "Inventory info about {0} skus missing",
                skuInfoList.size() - whInventory.getNumOfSkus());

        //load inventory details of the store, including what was sold in the previous cycle
        Map<String/*storeId*/, List<StoreInventoryInfo>> storeInventoryInfoMap = LoadProcessor
                .loadStoreInventoryInfo(new File(root, "storeInventory.csv"),
                        statics.getValidStoreIds(), statics.getValidSKUs());

        LOG.log(Level.INFO, "Loaded store inventory details for {0} stores from storeInventory.csv",
                storeInfoList.size());

        List<StoreModel> storeModels = storeInfoList.stream()
                .filter(storeInfo -> {
                    String storeId = storeInfo.getStoreId();
                    boolean valid = storeInventoryInfoMap.containsKey(storeId)
                            && psarMap.containsKey(storeId);
                    if (!valid)
                    {
                        LOG.log(Level.FINE,
                                "Not allocating store {0} as either inventory info or PSAR for store is missing",
                                storeId);
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

//        for (StoreModel storeModel : storeModels)
//        {
//            System.out.println("store " + storeModel.getStoreId() + " TotalGap = " + storeModel.getTotalGap());
//        }

        LOG.log(Level.INFO, "Allocating SKUs to {0} stores, whose gap is more than moq", storeModels.size());

//        storeModels.forEach(s -> {
//            System.out.println(s.getStoreId() + " -> toAllocate=" + s.getSkusToAllocate().size()
//                    + ", sale=" + s.getSale() + ", gap=" + s.getTotalGap() + ", grade=" + s.getGrade());
//        });

        int totalSkusToAllocate = storeModels.stream().mapToInt(s -> s.getSkusToAllocate().size()).sum();
        LOG.log(Level.INFO, "Total Skus to allocate : {0}", totalSkusToAllocate);

        Engine engine = new Engine(whInventory, storeModels, statics);
        List<StoreAllocation> allocate = engine.allocate();
        StoreProcessor.storeAllocations(new File(root, "allocations.csv"), allocate);

        LOG.log(Level.INFO, "Allocated {0} from {1} items",
                new Object[]{(whInventory.getInitialTotalInventory() - whInventory.getTotalInventory()),
                        whInventory.getInitialTotalInventory()});
    }
}
