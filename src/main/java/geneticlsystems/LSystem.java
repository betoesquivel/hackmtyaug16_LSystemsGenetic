package geneticlsystems;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import java.io.IOException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * Created by enrique on 27/08/16.
 */
public class LSystem {

    int angle;
    int iterations;
    String axiom;
    String [] rules;
    String constants;
    int rank;
    String id;

    public LSystem() {
        angle = 0;
        axiom = "";
        rules = new String [10];
        rules[0] ="";
        constants ="";
        iterations = 0;
        rank = 0;
    }

    public LSystem(int angle, int iterations, String axiom, String[] rules, String constants, int rank) {
        this.angle = angle;
        this.iterations = iterations;
        this.axiom = axiom;
        this.rules = rules;
        this.constants = constants;
        this.rank = rank;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public static String expand(LSystem l){
        String axiom = l.getAxiom();
        String rules [] = l.getRules();
        String letters ="";

        for(int i =0; i<rules.length; i++){
            letters+= rules[i].substring(0,1);
        }
        String prev = "";
        int i;
        for(i =0; i<20; i++) {
            for(int j =0; j<axiom.length(); j++) {
                int index = letters.indexOf(axiom.charAt(j));
                if (index >= 0) {
                    //letter, expand with rule
                    String r = rules[index].substring(2);
                    axiom = axiom.substring(0, j) + r + axiom.substring(j + 1);
                    j += r.length() - 1;
                }
            }
            if(axiom.length() == prev.length()){
                    if(axiom.equals(prev)){
                        break;
                    }
            }
            if(axiom.length()>70000){
                axiom = prev;
                System.out.println(axiom);
                System.out.println(i);
                i--;
                break;

            }
            prev = axiom;
        }
        l.setIterations(i);
        return axiom;
    }

    public static void saveLSystem(LSystem individual, DynamoDB dynamoDB) {

        Table table = dynamoDB.getTable("lsystems");

        String id = individual.getId();
        String axiom = individual.getAxiom();
        int angle = individual.getAngle();
        int iterations = individual.getIterations();
        String []rules = individual.getRules();
        String constants = individual.getConstants();
        int rank = individual.getRank();

        Item item = new Item()
                .withPrimaryKey("id", id)
                .withString("type", "design")
                .withString("axiom", axiom)
                .withNumber("angle", angle)
                .withNumber("iterations", iterations)
                .withList("rules", rules)
                .withString("constants", constants)
                .withNumber("rank", rank);
        
        PutItemOutcome outcome = table.putItem(item);

    }

    public static void uploadToS3(String id, String payload) {

        String bucketName     = "hackmtyaug16-bigben-lsystems";
        String uploadFileName = id+".txt";
        AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
        try {
            s3client.putObject( bucketName, uploadFileName, payload );
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println(
                    "Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
}
