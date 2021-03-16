package mark;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BugReader {

    public static List<BugInstance> readReport(String reportPath) {
        List<BugInstance> BugInstanceList = new ArrayList<>();
        log.info("Read xml: " + reportPath);
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(reportPath);
            for (Element e : document.getRootElement().elements("BugInstance")) {
                BugInstance bug = new BugInstance();
                bug.setType(e.attributeValue("type"));
                bug.setCategory(e.attributeValue("category"));
                if (e.element("SourceLine") == null) {
                    continue;
                }
                bug.setClassPath(e.element("SourceLine").attributeValue("sourcepath"));
                bug.setLine(Integer.parseInt(e.element("SourceLine").attributeValue("start")));
                String className = e.element("SourceLine").attributeValue("classname");
                String[] nameArray = className.split("\\.");
                bug.setClassName(nameArray[nameArray.length - 1]);
                for (Element methodElement : e.elements("Method")) {
                    if (className.equals(methodElement.attributeValue("classname"))) {
                        bug.setMethodName(methodElement.attributeValue("name"));
                        bug.setSignature(methodElement.attributeValue("signature"));
                    }
                }
                BugInstanceList.add(bug);
            }
        } catch (DocumentException e) {
            log.error(e.getMessage());
        }
        return BugInstanceList;
    }

    public static List<BugInstance> readReport(String reportDir, String version) {
        return BugReader.readReport(Paths.get(reportDir, version + ".xml").toString());
    }
    
}
