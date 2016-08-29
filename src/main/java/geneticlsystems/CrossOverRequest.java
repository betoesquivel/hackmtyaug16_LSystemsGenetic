package geneticlsystems;

/**
 * Created by enrique on 29/08/16.
 */
public class CrossOverRequest {

    LSystem parent1;
    LSystem parent2;

    public LSystem getParent2() {
        return parent2;
    }

    public void setParent2(LSystem parent2) {
        this.parent2 = parent2;
    }

    public LSystem getParent1() {

        return parent1;
    }

    public void setParent1(LSystem parent1) {
        this.parent1 = parent1;
    }

    public CrossOverRequest(LSystem parent1) {

        this.parent1 = parent1;
    }

    public CrossOverRequest() {

    }
}
