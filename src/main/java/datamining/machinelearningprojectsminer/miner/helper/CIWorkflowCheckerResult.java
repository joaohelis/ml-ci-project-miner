package datamining.machinelearningprojectsminer.miner.helper;

import java.util.ArrayList;
import java.util.List;

public class CIWorkflowCheckerResult {

    private boolean checkResult;
    private List<String> checkLogs;

    public CIWorkflowCheckerResult(){
        this(false);
    }
    
    public CIWorkflowCheckerResult(boolean checkResult) {
        this.checkResult = checkResult;
        this.checkLogs = new ArrayList<>();
    }

    public boolean getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(boolean checkResult) {
        this.checkResult = checkResult;
    }

    public List<String> getCheckLogs() {
        return checkLogs;
    }

    public void setCheckLogs(List<String> checkLogs) {
        this.checkLogs = checkLogs;
    }

    @Override
    public String toString() {
        String temp = "----------------------------------\n" +
                      "CIWorkflowCheckerResult\n" +
                      "----------------------------------\n" +
                      ">>> checkResult: {checkResult}\n".replace("{checkResult}", Boolean.toString(checkResult)) + 
                      "----------------------------------\n";                      
        for(String log: checkLogs){
            temp += "- " + log + "\n";
        }
        return temp;
    }    
}
