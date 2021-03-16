import lombok.extern.slf4j.Slf4j;
import mark.BugMarker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import util.CommandUtil;
import util.ConfigUtil;
import util.CsvUtil;
import util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class JarAnalyzer {

    // 保存阿里云镜像映射表
    private final static Map<String, String> mirrorMap = new HashMap<>();

    static {
        mirrorMap.put("https://repo1.maven.org/maven2/", "https://maven.aliyun.com/repository/central/");
        mirrorMap.put("http://jcenter.bintray.com/", "https://maven.aliyun.com/repository/public");
        mirrorMap.put("https://maven.google.com/", "https://maven.aliyun.com/repository/google");
        mirrorMap.put("http://repo.spring.io/libs-milestone/", "https://maven.aliyun.com/repository/spring");
    }

    private String mavenURL;
    private String projectName;
    private List<String> versions;

    /**
     * 根据Maven仓库地址初始化类，仅保留规整版本号的版本
     * 注意：
     * 1. Maven仓库地址为包含maven-metadata.xml文件的URL
     * 2. URL必须以'/'结尾
     * @param mavenURL Maven仓库地址
     */
    public JarAnalyzer(String mavenURL) {
        try {
            this.mavenURL = mavenURL;
            Path urlPath = Paths.get(mavenURL);
            this.projectName = urlPath.getName(urlPath.getNameCount() - 1).toString();
            versions = Jsoup.connect(mavenURL).get().select("a").stream()
                    .filter(e -> e.hasAttr("title"))
                    .map(e -> e.attr("title"))
                    .filter(s -> s.endsWith("/"))
                    .map(s -> s.substring(0, s.length() - 1))
                    .filter(s -> s.matches("[\\d+][\\.\\d+]*"))
                    .collect(Collectors.toList());
            log.info(String.format("Project Name: %s, Version Num: %d", projectName, versions.size()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载不同版本的源代码包和Jar包
     * 注意：
     * 1. 仅在当前版本有源代码的情况下才会下载两个包
     * 2. 下载前会自动清空用于保存的文件夹
     */
    public void download() {
        FileUtil.initDirectory(Paths.get(ConfigUtil.projectDir, projectName));
        // 可用版本计数
        int versionNum = 0;
        for (String v : versions) {
            String jarName = null;
            boolean hasSource = false;
            try {
                for (Element e : Jsoup.connect(mavenURL + v + "/").get().select("a")) {
                    if (e.hasAttr("title")) {
                        String name = e.attr("title");
                        if (name.endsWith(".jar")) {
                            if (jarName == null) {
                                jarName = name;
                            } else {
                                // 取以jar结尾的最短的文件名作为版本对应的jar包
                                jarName = jarName.length() < name.length() ? jarName : name;
                            }
                            if (name.contains("sources")) {
                                hasSource = true;
                                CommandUtil.executeDownload(useMirror(mavenURL + v + "/" + name), getPath(name));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 仅在当前版本有源代码的情况下才会下载Jar包
            if (hasSource) {
                versionNum++;
                CommandUtil.executeDownload(useMirror(mavenURL + v + "/" + jarName), getPath(jarName));
            }
        }
        // 输出可用版本占比
        log.info(String.format("Available Version Num: %d/%d", versionNum, versions.size()));
    }

    /**
     * 解压源代码包，解压后会删除压缩包
     */
    public void unzip() {
        try {
            Files.list(Paths.get(ConfigUtil.projectDir, projectName))
                    .map(Path::toFile)
                    .filter(f -> f.getName().contains("sources"))
                    .forEach(f -> {
                        String saveDirName = f.getName().replace("-sources.jar", "");
                        CommandUtil.executeUnzip(f.getPath(), getPath(saveDirName));
                        f.delete();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描各个版本的jar包生成扫描报告
     */
    public void scan() {
        FileUtil.initDirectory(Paths.get(ConfigUtil.reportDir, projectName));
        try {
            Files.list(Paths.get(ConfigUtil.projectDir, projectName))
                    .map(Path::toFile)
                    .filter(f -> f.isFile() && f.getName().endsWith(".jar"))
                    .forEach(f -> {
                        String fileName = f.getName().replace(".jar", ".xml");
                        CommandUtil.executeFindBugs(f.getPath(),
                                Paths.get(ConfigUtil.reportDir, projectName, fileName).toString());
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void analyse() {
        List<String[]> result = BugMarker.markWithVersions(Paths.get(ConfigUtil.projectDir, projectName).toString(),
                Paths.get(ConfigUtil.reportDir, projectName).toString());
        CsvUtil.saveStrings(result, Paths.get(ConfigUtil.resultDir, projectName + ".csv").toString());
    }

    private String useMirror(String originURL) {
        for (Map.Entry<String, String> entry : mirrorMap.entrySet()) {
            if (originURL.contains(entry.getKey())) {
                return originURL.replace(entry.getKey(), entry.getValue());
            }
        }
        return originURL;
    }

    private String getPath(String childPath) {
        return Paths.get(ConfigUtil.projectDir, projectName, childPath).toString();
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            if ("--all-steps".equals(args[0]) || "--only-download".equals(args[0])) {
                JarAnalyzer downloader = new JarAnalyzer(args[1]);
                downloader.download();
                downloader.unzip();
                if ("--only-download".equals(args[0])) {
                    return;
                }
                downloader.scan();
                downloader.analyse();
            } else if ("--only-analyse".equals(args[0])) {

            }
        }
    }
}
