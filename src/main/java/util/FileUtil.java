package util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileUtil {

    /**
     * 初始化文件夹：如果存在先删除文件夹，然后创建
     * @param path 需要创建的文件夹路径
     */
    public static void initDirectory(Path path) {
        try {
            File targetPath = path.toFile();
            if (targetPath.exists()) {
                FileUtils.deleteDirectory(targetPath);
            }
            Files.createDirectory(path);
        } catch (Exception e) {
            log.error("Init Directory Error: " + path.toString());
            e.printStackTrace();
        }
    }

}
