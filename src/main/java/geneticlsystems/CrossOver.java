package geneticlsystems;

import java.io.IOException;

import java.util.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;

import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public class CrossOver implements RequestHandler<CrossOverRequest, CrossOverResponse> {

    public static LSystem crossOver(LSystem parent1, LSystem parent2){
        Random r = new Random();

        //Angle crossover
        int angleP1 = parent1.getAngle();
        int angleP2 = parent2.getAngle();

        String angleP1Str = Integer.toBinaryString(angleP1);
        String angleP2Str = Integer.toBinaryString(angleP2);

        while(angleP1Str.length()<9){
            angleP1Str = "0"+angleP1Str;
        }
        while(angleP2Str.length()<9){
            angleP2Str = "0"+angleP2Str;
        }


        int index = r.nextInt(10);

        String newAngleStr = angleP1Str.substring(0, index)
                + angleP2Str.substring(index, angleP2Str.length());

        int newAngle = Integer.parseInt(newAngleStr, 2);

        //iteration crossover
        int newIterations = parent1.getIterations();

        //Axiom crossover
        String axiomP1 = parent1.getAxiom();
        String axiomP2 = parent2.getAxiom();

        String newAxiom;
        if(axiomP1.length() >= axiomP2.length()){
            index = r.nextInt(axiomP2.length()+1);
            newAxiom = axiomP2.substring(0,index)
                    + axiomP1.substring(index, axiomP1.length());
        }
        else{
            index = r.nextInt(axiomP1.length());
            newAxiom = axiomP1.substring(0,index)
                    + axiomP2.substring(index, axiomP2.length());
        }



        //Rules crossover
        String rulesP1[] = parent1.getRules();
        String rulesP2[] = parent2.getRules();

        String newRules [] = new String [rulesP1.length];

        for(int i=0; i<rulesP1.length; i++){
            newRules[i]="";
            if(rulesP1[i].length() >= rulesP2[i].length()+1){
                index = r.nextInt(rulesP2[i].length());
                newRules[i] = rulesP2[i].substring(0,index)
                    + rulesP1[i].substring(index, rulesP1[i].length());
            }
            else{
                index = r.nextInt(rulesP1[i].length()+1);
                newRules[i] = rulesP1[i].substring(0,index)
                    + rulesP2[i].substring(index, rulesP2[i].length());
            }
        }

        //Constants crossover
        String newConstants = parent1.getConstants();
        return new LSystem(newAngle, newIterations, newAxiom, newRules, newConstants, -10);

    }

    @Override
    public CrossOverResponse handleRequest(CrossOverRequest input, Context context) {

        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(Regions.EU_WEST_1));
        DynamoDB dynamoDB = new DynamoDB(client);

        //initialize
        LSystem l = crossOver(input.getParent1(), input.getParent2());
        l.setId( UUID.randomUUID().toString() );

        //write do dynamo
        LSystem.saveLSystem( l, dynamoDB );

        //expand
        String g_commands = l.getAngle() + ":" + LSystem.expand(l);
        //write to s3
        LSystem.uploadToS3( l.getId(), g_commands );

        return new CrossOverResponse( l.getId() );

    }
}
