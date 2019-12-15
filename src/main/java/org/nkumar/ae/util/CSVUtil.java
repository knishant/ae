package org.nkumar.ae.util;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public final class CSVUtil
{
    private CSVUtil()
    {
    }

    public static <T> List<T> loadCSV(File path, Class<? extends T> type)
    {
        try (CSVReader r = new CSVReader(new FileReader(path)))
        {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(r)
                    .withType(type)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            return csvToBean.parse();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
