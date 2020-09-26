package org.nkumar.ae.input;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import org.nkumar.ae.converter.GenderBeanField;
import org.nkumar.ae.model.Gender;
import org.nkumar.ae.model.Keyed;
import org.nkumar.ae.model.PrimaryStockAllocationRatio;
import org.nkumar.ae.model.SKUInfo;
import org.nkumar.ae.model.StoreInfo;
import org.nkumar.ae.model.StoreInventoryInfo;
import org.nkumar.ae.model.WarehouseInventoryInfo;
import org.nkumar.ae.util.CSVUtil;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.nkumar.ae.util.StringUtil.codify;

public final class LoadProcessor
{
    private static final Logger LOG = Logger.getLogger(LoadProcessor.class.getName());

    private LoadProcessor()
    {
    }

    public static Map<String/*storeId*/, PrimaryStockAllocationRatio> loadPSAR(File path, Set<String> validStoreIds)
    {
        Map<String, PrimaryStockAllocationRatio> map = new TreeMap<>();
        CountingInvalidPredicate<PSARRow> validKeyPredicate = new CountingInvalidPredicate<>(
                validStoreIds, PSARRow::getStoreId,
                "Ignoring PSAR as its storeId ({0}) is not defined in storeinfo list");
        CSVUtil.loadCSV(path, PSARRow.class).stream()
                .filter(validKeyPredicate)
                .forEach(row -> {
                    PrimaryStockAllocationRatio ratio = map
                            .computeIfAbsent(row.getStoreId(), storeId -> new PrimaryStockAllocationRatio());
                    ratio.setQuantity(row.getGender(), row.getShape(), row.getQuantity());
                });
        validKeyPredicate.logIfCountNonZero("Ignored {0} stores in psar as they are not defined in storeinfo");
        return map;
    }

    private static final class CountingInvalidPredicate<T> implements Predicate<T>
    {
        private final AtomicInteger count = new AtomicInteger(0);

        private final Set<String> validIds;
        private final Function<T, String> keyExtractor;
        private final String msg;

        private CountingInvalidPredicate(Set<String> validIds, Function<T, String> keyExtractor, String msg)
        {
            this.validIds = validIds;
            this.keyExtractor = keyExtractor;
            this.msg = msg;
        }

        @Override
        public boolean test(T t)
        {
            String key = keyExtractor.apply(t);
            boolean contains = validIds.contains(key);
            if (!contains)
            {
                count.incrementAndGet();
                LOG.log(Level.WARNING, msg, key);
            }
            return contains;
        }

        private void logIfCountNonZero(String msg)
        {
            if (count.get() > 0)
            {
                LOG.log(Level.WARNING, msg, count.get());
            }
        }
    }


    public static WarehouseInventoryInfo loadWarehouseInventoryInfo(File path, Set<String> validSKUs)
    {
        CountingInvalidPredicate<WarehouseInventoryRow> validKeyPredicate = new CountingInvalidPredicate<>(
                validSKUs, WarehouseInventoryRow::getSKU,
                "Ignoring warehouse inventory row as sku ({0}) is not defined in skuinfo list");
        Map<String, Integer> map = CSVUtil.loadCSV(path, WarehouseInventoryRow.class).stream()
                .filter(validKeyPredicate)
                .collect(Collectors.toMap(WarehouseInventoryRow::getSKU, WarehouseInventoryRow::getAvailable));
        validKeyPredicate.logIfCountNonZero(
                "Ignored {0} skus in warehouse inventory as they are not defined in skuinfo");
        return new WarehouseInventoryInfo(map);
    }

    public static Map<String/*storeId*/, List<StoreInventoryInfo>> loadStoreInventoryInfo(File path,
            Set<String> validStoreIds, Set<String> validSKUs)
    {
        CountingInvalidPredicate<StoreInventoryInfo> validKeyPredicate1 = new CountingInvalidPredicate<>(
                validStoreIds, StoreInventoryInfo::getStoreId,
                "Ignoring store inventory as storeId ({0}) is not defined in storeinfo list");

        CountingInvalidPredicate<StoreInventoryInfo> validKeyPredicate2 = new CountingInvalidPredicate<>(
                validSKUs, StoreInventoryInfo::getSKU,
                "Ignoring store inventory as sku ({0}) is not defined in skuinfo list");

        Map<String, List<StoreInventoryInfo>> collect = CSVUtil.loadCSV(path, StoreInventoryInfo.class).stream()
                .filter(validKeyPredicate1)
                .filter(validKeyPredicate2)
                .collect(Collectors.groupingBy(StoreInventoryInfo::getStoreId));

        validKeyPredicate1.logIfCountNonZero(
                "Ignored {0} stores in store inventory as they are not defined in storeinfo");

        validKeyPredicate2.logIfCountNonZero(
                "Ignored {0} skus in store inventory as they are not defined in skuinfo");
        return collect;
    }

    public static List<StoreInfo> loadStoreInfo(File path)
    {
        return CSVUtil.loadCSV(path, StoreInfo.class);
    }

    public static List<SKUInfo> loadSKU(File path)
    {
        return CSVUtil.loadCSV(path, SKUInfo.class);
    }

    public static final class PSARRow implements Keyed
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

        private int getQuantity()
        {
            return quantity;
        }

        public void setQuantity(int quantity)
        {
            this.quantity = quantity;
        }

        @Override
        public String getKey()
        {
            return String.join(",", getStoreId(), getGender().toString(), getShape());
        }
    }

    public static final class WarehouseInventoryRow implements Keyed
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

        private int getAvailable()
        {
            return available;
        }

        public void setAvailable(int available)
        {
            this.available = available;
        }

        @Override
        public String getKey()
        {
            return getSKU();
        }
    }

}
