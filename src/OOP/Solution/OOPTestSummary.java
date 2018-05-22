package OOP.Solution;

import OOP.Provided.OOPResult;
import OOP.Provided.OOPResult.*;
import java.util.*;

public class OOPTestSummary {
    public Map<String, OOPResult> testmap;

    public OOPTestSummary(Map<String, OOPResult> testmap){
        this.testmap=testmap;
    }

    public int getNumSuccesses(){
        return (int)testmap.entrySet().stream().filter(res->res.getValue().getResultType()== OOPTestResult.SUCCESS).count();
    }

    public int getNumFailures(){
        return (int)testmap.entrySet().stream().filter(res->res.getValue().getResultType()== OOPTestResult.FAILURE).count();
    }

    public int getNumExceptionMismatches(){
        return (int)testmap.entrySet().stream().filter(res->res.getValue().getResultType()== OOPTestResult.EXPECTED_EXCEPTION_MISMATCH).count();
    }

    public int getNumErrors(){
        return (int)testmap.entrySet().stream().filter(res->res.getValue().getResultType()== OOPTestResult.ERROR).count();
    }
}
