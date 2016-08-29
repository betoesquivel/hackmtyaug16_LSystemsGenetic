package geneticlsystems;

/**
 * Created by enrique on 29/08/16.
 */
public class MutateResponse {
    String offspringID;

    public MutateResponse() {
        offspringID = "";
    }

    public MutateResponse(String offspringID) {
        this.offspringID = offspringID;
    }

    public String getOffspringID() {
        return offspringID;
    }

    public void setOffspringID(String offspringID) {
        this.offspringID = offspringID;
    }
}
