package mark;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BugMarker {

    private final static Pattern pattern = Pattern.compile("[\\d+][.\\d+]*");

    /**
     * 使用不同版本的源代码标记正误报
     * @param projectDir 项目文件夹
     * @param reportDir 扫描报告文件夹
     * @return 需要保存的数据
     */
    public static List<String[]> markWithVersions(String projectDir, String reportDir) {
        List<String[]> result = new ArrayList<>();
        List<String> versionList = new ArrayList<>();
        // 检查版本号能不能找到对应的项目
        for (File file : new File(reportDir).listFiles()) {
            if (file.getName().endsWith(".xml")) {
                String versionName = file.getName().replace(".xml", "");
                if (Files.exists(Paths.get(projectDir, versionName))) {
                    versionList.add(versionName);
                }
            }
        }
        Collections.sort(versionList);
        log.info("Version Order: " + String.join(" ", versionList));

        List<BugInstance> preBugList = new ArrayList<>();
        for (int i = 0; i < versionList.size(); i++) {
            List<BugInstance> newBugList = BugReader.readReport(reportDir, versionList.get(i));
            if (preBugList.size() > 0) {
                for (BugInstance bug : preBugList) {
                    if (! newBugList.contains(bug)) {
                        Path path = Paths.get(projectDir, versionList.get(i), "src/main/java", bug.getClassPath());
                        boolean isExist = false;
                        if (Files.exists(path)) {
                            try {
                                CompilationUnit cu = StaticJavaParser.parse(path);
                                for (ClassOrInterfaceDeclaration cd : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                                    if (cd.getNameAsString().equals(bug.getClassName())) {
                                        for (MethodDeclaration md: cd.findAll(MethodDeclaration.class)) {
                                            if (md.getNameAsString().equals(bug.getMethodName())) {
                                                isExist = true;
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (isExist) bug.setState(BugInstance.TRUE_POSITIVE);
                    } else {
                        bug.setState(BugInstance.FALSE_POSITIVE);
                    }
                    List<String> line = bug.toLineString();
                    line.add(0,matchVersion(versionList.get(i-1)) + " | " + matchVersion(versionList.get(i)));
                    result.add(line.toArray(new String[line.size()]));
                }
            }
            preBugList = newBugList;
        }
        return result;
    }

    /**
     * 提取版本号
     * @param versionName 完整的版本名
     * @return 版本号
     */
    private static String matchVersion(String versionName) {
        Matcher matcher = pattern.matcher(versionName);
        return matcher.find() ? matcher.group(0) : versionName;
    }

}
