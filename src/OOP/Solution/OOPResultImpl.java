package OOP.Solution;
import OOP.Provided.OOPResult;

public class OOPResultImpl implements OOPResult{
    private OOPTestResult res;
    private String msg;
    public OOPResultImpl(OOPTestResult res, String msg){
        this.res=res;
        this.msg=msg;
    }

    public OOPTestResult getResultType(){
        return res;
    }

    public String getMessage(){
        return msg;
    }

    public boolean equals(Object obj){
        if(obj == null) return false;
        if(obj.getClass()!=this.getClass())
            return false;
        return getResultType()==((OOPResultImpl)obj).getResultType() && getMessage().equals(((OOPResultImpl)obj).getMessage());
    }
}
