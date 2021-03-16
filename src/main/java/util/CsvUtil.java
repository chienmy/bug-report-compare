package util;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvUtil {

    /**
     * 使用CSVWriter保存List<String[]>
     * @param data 字符串数据
     * @param savePath 保存路径
     */
    public static void saveStrings(List<String[]> data, String savePath) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(savePath));
            writer.writeAll(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
