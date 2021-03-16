package util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class CommandUtil {

    /**
     * 使用wget命令下载文件
     * @param url 文件URL
     * @param savePath 保存地址（指定到文件名）
     */
    public static void executeDownload(String url, String savePath) {
        log.info("DownLoad URL: " + url);
        String[] command = new String[]{"wget", url, "-O", savePath};
        execute(command);
    }

    /**
     * 使用unzip命令解压Jar包
     * @param jarPath jar包路径
     * @param savePath 解压后文件夹路径
     */
    public static void executeUnzip(String jarPath, String savePath) {
        log.info("Unzip: " + jarPath);
        String[] command = new String[]{"unzip -q", jarPath, "-d", savePath};
        execute(command);
    }

    /**
     * 使用findBugs扫描Jar包
     * @param jarPath Jar包路径
     * @param xmlPath xml保存路径
     */
    public static void executeFindBugs(String jarPath, String xmlPath) {
        log.info("FindBugs scan: " + jarPath);
        String[] command = new String[]{ConfigUtil.findBugsPath,
                "-textui -jvmArgs -Xmx2048m -high -sortByClass -xml -output",
                xmlPath, jarPath};
        execute(command);
    }

    /**
     * 执行Shell命令
     * @param commands 需要执行的命令
     */
    private static void execute(String[] commands) {
        try {
            Process pro = Runtime.getRuntime().exec(String.join(" ", commands));
            String info = executeResult(new BufferedReader(new InputStreamReader(pro.getInputStream())));
            String error = executeResult(new BufferedReader(new InputStreamReader(pro.getErrorStream())));
            if (ConfigUtil.commandShow && !info.trim().isEmpty()) {
                log.info(info);
            }
            if (ConfigUtil.commandShow && !error.trim().isEmpty()) {
                log.error(error);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从BufferedReader中获取命令执行的输出
     * @param br BufferedReader
     * @return 输出结果
     * @throws IOException IO异常
     */
    private static String executeResult(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

}
