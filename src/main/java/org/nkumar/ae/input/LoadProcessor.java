package org.nkumar.ae.input;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import org.nkumar.ae.converter.GenderBeanField;
import org.nkumar.ae.model.Gender;
import org.nkumar.ae.model.PrimaryStockAllocationRatio;
import org.nkumar.ae.model.StoreInfo;
import org.nkumar.ae.model.StoreInventoryInfo;
import org.nkumar.ae.model.WarehouseInventoryInfo;
import org.nkumar.ae.util.CSVUtil;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.nkumar.ae.util.StringUtil.codify;

public final class LoadProcessor
{
    private LoadProcessor()
    {
    }

    public static Map<String/*storeId*/, PrimaryStockAllocationRatio> loadPSAR(File path)
    {
        Map<String, PrimaryStockAllocationRatio> map = new TreeMap<>();
        CSVUtil.loadCSV(path, PSARRow.class).forEach(row -> {
            PrimaryStockAllocationRatio ratio = map
                    .computeIfAbsent(row.getStoreId(), storeId -> new PrimaryStockAllocationRatio());
            ratio.setQuantity(row.getGender(), row.getShape(), row.getQuantity());
        });
        return map;
    }

    public static WarehouseInventoryInfo loadWarehouseInventoryInfo(File path)
    {
        Map<String, Integer> map = CSVUtil.loadCSV(path, WarehouseInventoryRow.class).stream()
                .collect(Collectors.toMap(WarehouseInventoryRow::getSKU, WarehouseInventoryRow::getAvailable));
        return new WarehouseInventoryInfo(map);
    }

    public static Map<String/*storeId*/, List<StoreInventoryInfo>> loadStoreInventoryInfo(File path)
    {
        return CSVUtil.loadCSV(path, StoreInventoryInfo.class).stream()
                .collect(Collectors.groupingBy(StoreInventoryInfo::getStoreId));
    }

    public static List<StoreInfo> loadStoreInfo(File path)
    {
        return CSVUtil.loadCSV(path, StoreInfo.class);
    }

    private static final class PSARRow
    {
        @CsvBindByName
        private String storeId;
        @CsvCustomBindByName(converter = GenderBeanField.class)
        private Gender gender;
        @CsvBindByName
        private String shape;
        @CsvBindByName
        private int quantity;

        public String getStoreId()
        {
            return storeId;
        }

        public void setStoreId(String storeId)
        {
            this.storeId = storeId;
        }

        public Gender getGender()
        {
            return gender;
        }

        public void setGender(Gender gender)
        {
            this.gender = gender;
        }

        public String getShape()
        {
            return shape;
        }

        public void setShape(String shape)
        {
            this.shape = shape;
        }

        public int getQuantity()
        {
            return quantity;
        }

        public void setQuantity(int quantity)
        {
            this.quantity = quantity;
        }
    }

    private static final class WarehouseInventoryRow
    {
        @CsvBindByName
        private String SKU;
        @CsvBindByName
        private int available;

        public String getSKU()
        {
            return SKU;
        }

        public void setSKU(String SKU)
        {
            this.SKU = codify(SKU);
        }

        public int getAvailable()
        {
            return available;
        }

        public void setAvailable(int available)
        {
            this.available = available;
        }
    }

}
