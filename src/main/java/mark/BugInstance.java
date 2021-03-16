package mark;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class BugInstance {

    public final static String UNKNOWN = "unknown";
    public final static String FALSE_POSITIVE = "fp";
    public final static String TRUE_POSITIVE = "tp";

    private String classPath = "";
    private String className = "";
    private String methodName = "";
    private String signature = "";
    private String type = "";
    private String category = "";
    private int line = -1;
    private String state = BugInstance.UNKNOWN;

    public boolean equals(Object o) {
        if (! (o instanceof BugInstance)) return false;
        BugInstance bug = (BugInstance) o;
        return this.classPath.equals(bug.getClassPath()) &&
                this.className.equals(bug.getClassName()) &&
                this.methodName.equals(bug.getMethodName()) &&
                this.signature.equals(bug.getSignature()) &&
                this.type.equals(bug.getType()) &&
                this.category.equals(bug.getCategory());
    }

    public List<String> toLineString() {
        return new ArrayList<>(Arrays.asList(type, classPath, methodName, signature, "" + line, state));
    }

}
