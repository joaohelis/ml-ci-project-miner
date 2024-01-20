package datamining.machinelearningprojectsminer.config;

import java.util.Arrays;
import java.util.List;

import datamining.machinelearningprojectsminer.miner.helper.GHToken;

public class  Config {

    public static String codecovToken = "PUT YOUR CODECOV TOKEN HERE";

    public static String coverallsToken = "PUT YOUR COVERALLS TOKEN HERE";    

    public static String travisToken = "PUT YOUR TRAVIS TOKEN HERE";

    public static List<GHToken> gitHubTokens = Arrays.asList(new GHToken[]{        
                
        new GHToken("PUT YOUR GITHUB TOKEN HERE"),
        // new GHToken("PUT YOUR GITHUB TOKEN HERE"),
    });   

}
