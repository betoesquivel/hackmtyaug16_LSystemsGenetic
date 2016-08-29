package geneticlsystems;

/**
 * Created by enrique on 29/08/16.
 */
public class MutateRequest {
    private LSystem parent;

    public MutateRequest(){
        parent = new LSystem();
    }

    public MutateRequest(LSystem parent) {
        this.parent = parent;
    }

    public LSystem getParent() {
        return parent;
    }

    public void setParent(LSystem parent) {
        this.parent = parent;
    }
}
