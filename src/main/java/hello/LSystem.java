package hello;

/**
 * Created by enrique on 27/08/16.
 */
public class LSystem {

    int angle;
    int iterations;
    String axiom;
    String [] rules;
    String constants;

    public LSystem() {
        angle = 0;
        axiom = "";
        rules = new String [10];
        rules[0] ="";
        constants ="";
        iterations = 0;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public String getConstants() {
        return constants;
    }

    public void setConstants(String constants) {
        this.constants = constants;
    }

    public LSystem(int angle, int iterations, String axiom, String[] rules, String constants) {
        this.angle = angle;
        this.iterations = iterations;
        this.axiom = axiom;
        this.rules = rules;
        this.constants = constants;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public String getAxiom() {
        return axiom;
    }

    public void setAxiom(String axiom) {
        this.axiom = axiom;
    }

    public String[] getRules() {
        return rules;
    }

    public void setRules(String[] rules) {
        this.rules = rules;
    }
}
