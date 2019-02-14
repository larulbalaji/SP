package sailpoint.seri.tools.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;

public class ConditionLessThan implements Condition {

    private double arg1;
    private double arg2;

    public double getArg1() {
        return arg1;
    }

    public void setArg1(double arg1) {
        this.arg1 = arg1;
    }

    public double getArg2() {
        return arg2;
    }

    public void setArg2(double arg2) {
        this.arg2 = arg2;
    }

    public boolean eval() throws BuildException {
        return (arg1 < arg2);
    }
}
