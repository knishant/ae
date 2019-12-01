package org.nkumar.ae.input;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import org.nkumar.ae.converter.GenderBeanField;
import org.nkumar.ae.model.Gender;
import org.nkumar.ae.model.PrimaryStockAllocationRatio;
import org.nkumar.ae.model.StoreInfo;
import org.nkumar.ae.model.StoreInventoryInfo;
import org.nkumar.ae.model.WarehouseInventoryInfo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        try (CSVReader r = new CSVReader(new FileReader(path)))
        {
            CsvToBean<PSARRow> csvToBean = new CsvToBeanBuilder<PSARRow>(r)
                    .withType(PSARRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            List<PSARRow> list = csvToBean.parse();
            for (PSARRow row : list)
            {
                PrimaryStockAllocationRatio ratio = map
                        .computeIfAbsent(row.getStoreId(), storeId -> new PrimaryStockAllocationRatio());
                ratio.setQuantity(row.getGender(), row.getShape(), row.getQuantity());
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static WarehouseInventoryInfo loadWarehouseInventoryInfo(File path)
    {
        try (CSVReader r = new CSVReader(new FileReader(path)))
        {
            CsvToBean<WarehouseInventoryRow> csvToBean = new CsvToBeanBuilder<WarehouseInventoryRow>(r)
                    .withType(WarehouseInventoryRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            Map<String, Integer> map = csvToBean.parse().stream()
                    .collect(Collectors.toMap(WarehouseInventoryRow::getSKU, WarehouseInventoryRow::getAvailable));
            return new WarehouseInventoryInfo(map);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Map<String/*storeId*/, List<StoreInventoryInfo>> loadStoreInventoryInfo(File path)
    {
        try (CSVReader r = new CSVReader(new FileReader(path)))
        {
            CsvToBean<StoreInventoryInfo> csvToBean = new CsvToBeanBuilder<StoreInventoryInfo>(r)
                    .withType(StoreInventoryInfo.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            return csvToBean.parse().stream()
                    .collect(Collectors.groupingBy(StoreInventoryInfo::getStoreId));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    public static List<StoreInfo> loadStoreInfo(File path)
    {
        try (CSVReader r = new CSVReader(new FileReader(path)))
        {
            CsvToBean<StoreInfo> csvToBean = new CsvToBeanBuilder<StoreInfo>(r)
                    .withType(StoreInfo.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            return csvToBean.parse();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

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
