package geneticlsystems;

import java.io.IOException;
import java.util.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

public class Handler implements RequestHandler<Request, Response> {

    public static LSystem initialize(){
        //generates a random individual
        //initialize angle
        Random r = new Random();
        int angle = r.nextInt(360);

        //initialize axiom
        String axiom = "";
        String letters [] = {"X", "Y", "Z"};
        String symbols [] = {"+", "-"};
        axiom += letters[r.nextInt(letters.length)];
        int len = r.nextInt(5)+1;
        for(int i=0; i<len; i++){
            if(r.nextBoolean()){
                //letter
                int index = r.nextInt(letters.length);
                axiom+=letters[index];
            }
            else{
                //symbol
                axiom+=symbols[r.nextInt(symbols.length)];
            }
        }

        //initialize rules
        String rules [] = new String [letters.length];
        for(int s =0; s<letters.length; s++){
            String rule = "";
            rule+=letters[s];
            rule+="=";
            len = r.nextInt(5)+1;
            for(int i=0; i<len; i++){
                if(r.nextBoolean()){
                    //letter
                    int index = r.nextInt(letters.length);
                    rule+=letters[index];
                }
                else{
                    //symbol
                    rule+=symbols[r.nextInt(symbols.length)];
                }
            }
            rules[s] = rule;
        }
        LSystem l = new LSystem(angle, 5, axiom, rules, "");
        return l;
    }

    public static LSystem mutation(LSystem parent){
        double mutationChance = 0.1;

        String letters [] = {"X", "Y", "Z"};
        String symbols [] = {"+", "-"};

        //mutate angle
        Random r = new Random();
        int angle = parent.getAngle();
        String angleStr = Integer.toBinaryString(angle);
        while(angleStr.length() < 9){
            angleStr = "0"+angleStr;
        }
        String newAngle = "";
        for(int i =0; i<angleStr.length(); i++){
            if(r.nextDouble()<mutationChance){
                if(angleStr.charAt(i) == '0'){
                    newAngle +="1";
                }
                else{
                    newAngle +="0";
                }
            }
            else{
                newAngle+=angleStr.substring(i, i+1);
            }
        }

        //mutate iterations
        int iterations = parent.getIterations();
        int newIterations = iterations;

        //mutate axiom
        String axiom = parent.getAxiom();
        String newAxiom = "";
        for(int i=0; i<axiom.length(); i++) {
            if(r.nextDouble()<mutationChance){
                //mutate character
                if(r.nextBoolean())
                    newAxiom+=letters[r.nextInt(symbols.length)];
                else
                    newAxiom+=symbols[r.nextInt(symbols.length)];
            }
            else{
                newAxiom += axiom.substring(i, i+1);
            }
        }

        //check that axiom has a letter, if no: add one at the begining
        boolean foundLetter = false;
        for(int i =0; i<letters.length; i++){
            if(newAxiom.contains(letters[i])){
                foundLetter = true;
            }
        }
        if(!foundLetter){
            newAxiom+=letters[r.nextInt(letters.length)];
        }

        //mutate rules
        String rules [] = parent.getRules();
        String newRules [] = new String [rules.length];
        for(int i=0; i<rules.length; i++){
            newRules[i] = rules[i].substring(0,2);
            for(int j=2; j<rules[i].length(); j++){
                if(r.nextDouble()<mutationChance){
                    //mutate character
                    if(r.nextBoolean()){
                        newRules[i]+=letters[r.nextInt(letters.length)];
                    }
                    else{
                        newRules[i]+=symbols[r.nextInt(symbols.length)];
                    }
                }
                else{
                    newRules[i]+=rules[i].substring(j, j+1);
                }
                if(r.nextDouble()<(mutationChance*2.0)){
                    j--;
                }
                if(r.nextDouble()<(mutationChance*2.0)){
                    j++;
                }
            }
        }

        //mutate constants
        String constants = parent.getConstants();
        String newConstants = constants;

        return new LSystem(Integer.parseInt(newAngle, 2), newIterations, newAxiom, newRules, newConstants);
    }

	@Override
	public Response handleRequest(Request input, Context context) {

        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(Regions.EU_WEST_1));
        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("lsystems");

        ArrayList <LSystem> pop = new ArrayList<LSystem>();
        for(int i=0; i<5; i++) {
            pop.add(initialize());

            int angle = pop.get(i).getAngle();
            int iterations = pop.get(i).getIterations();
            String axiom = pop.get(i).getAxiom();
            String rules [] = pop.get(i).getRules();

            String id = UUID.randomUUID().toString();
            Item item = new Item()
                .withPrimaryKey("id", id)
                .withString("type", "design")
                .withString("axiom", axiom)
                .withNumber("angle", angle)
                .withNumber("iterations", iterations)
                .withList("rules", rules);

            PutItemOutcome outcome = table.putItem(item);

            System.out.println("\nIndividual "+i+":");
            System.out.println("Angle: "+ angle);
            System.out.println("Axiom: "+ axiom);
            for(int j =0; j < rules.length; j++) {
                System.out.print("Rule "+j+": ");
                System.out.println(rules[j]);
            }
        }
		return new Response("Go Serverless cessfully!", input);
  }
}
