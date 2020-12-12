import twitter4j.*;

import java.util.Arrays;
import java.util.Date;
import java.util.logging.*;
import java.util.logging.Logger;

/**
 * A bot that automatically detects giveaways run by teams and tweets them out.
 *
 * @author Nate S.
 */
public class Main {
    private static Twitter me; /* The twitter defined in twitter4j.properties */
    private static boolean detected = false; /* A boolean to prevent duplicate occurrences of keywords in statusTexts */
    private static boolean verified = false; /* A boolean to verify that the flagged tweet is posted by a username in String[] handles */
    private static String type = "Unknown"; /* The default 'Type' of tweet */

    /**
     * The main method. Initializes Twitter, TwitterStream, and the StatusListener.
     *
     * @param args the command line arguments
     * @throws TwitterException
     */
    public static void main(String[] args) throws TwitterException {
        System.out.println("[" + java.util.Calendar.getInstance().getTime() + "]Running main");
        me = TwitterFactory.getSingleton();
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();

        /* The handles to filter through */
        String[] handles = {"Hangzhou_Spark", "ChengduHunters", "GZCharge", "ATLReign", "ParisEternal",
                "TorontoDefiant", "Outlaws", "FLMayhem", "SeoulDynasty", "LAGladiators",
                "NYXL", "Fusion", "Spitfire", "DallasFuel", "LAValiant",
                "ShanghaiDragons", "BostonUprising", "SFShock", "washjustice", "VancouverTitans"
        };

        System.out.println("[" + java.util.Calendar.getInstance().getTime() + "]With handles " + Arrays.toString(handles));

        /* The keywords to filter through */
        final String[] keyWords = {"giveaway", "copy", "free copy", "Overwatch Origins Edition", "for PC", "receive a",
                "Origins Edition", "tokens", "skins", "skin code", "spray code"};

        System.out.println("[" + java.util.Calendar.getInstance().getTime() + "]With keywords " + Arrays.toString(keyWords));

        /* An array to store the handles to filter through, as long userId's */
        final long[] teams = new long[handles.length];
        for (int i = 0; i < handles.length; i++) {
            User account = me.showUser(handles[i]);
            teams[i] = account.getId();
        }

        /**
         * A listener to detect status updates (new tweets)
         * @param status Information of the tweet (author, text, etc.)
         */
        StatusListener statusListener = new StatusAdapter() {
            @Override
            public void onStatus(Status status) {
                String statusText = status.getText();
                for (String keyword : keyWords) {
                    if (statusText.toLowerCase().contains(keyword) && !detected) { /* Prevents duplicates and case sensitivity */
                        for (long id : teams) {                                    /* Prevents replies from being flagged */
                            if (id == status.getUser().getId())
                                verified = true;
                        }
                        if (verified) {
                            System.out.println("User: \n\t@" + status.getUser().getScreenName());
                            System.out.println("Tweet: \n\t" + status.getText() + "\n=========================");
                            detected = true;
                            type = findType(status);
                            try {
                                publishTweet(status);
                            } catch (TwitterException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                detected = false;
                verified = false;
            }
        };
        twitterStream.addListener(statusListener);
        FilterQuery filter = new FilterQuery();
        filter.follow(teams);
        twitterStream.filter(filter);
    }

    /**
     * Finds the type of promotion being hosted.
     *
     * @param contents The contents of the tweet, used to find the type of promotion
     * @return The type of promotion
     */
    public static String findType(Status contents) {
        String statusText = contents.getText().toLowerCase();
        if (statusText.contains("copy"))
            type = "Free Copy";
        else if (statusText.contains("overwatch origins edition"))
            type = "Free Copy";
        else if (statusText.contains("tokens") || statusText.contains("token"))
            type = "Token";
        else if (statusText.contains("skin code"))
            type = "Skin Code";
        else if (statusText.contains("skins"))
            type = "Skin Code";
        else if (statusText.contains("contenders") || statusText.contains("contender"))
            type = "Contenders";
        else if (statusText.contains("spray code"))
            type = "Spray Code";
        else
            type = "Unknown";
        if (statusText.contains("giveaway") || statusText.contains("will win"))
            type += " Giveaway";
        return type;
    }

    /**
     * Publishes a tweet to the provided Twitter
     *
     * @param contents The contents of the tweet.
     * @throws TwitterException
     */
    public static void publishTweet(Status contents) throws TwitterException {
        String url;
        if (contents.getURLEntities().length != 0)
            url = contents.getURLEntities()[0].getURL();
        else
            url = "N/A";
        me.updateStatus(
                "The " + contents.getUser().getName() + " are running a promotion." +
                        "\nLink: " + url +
                        "\nType: " + type +
                        "\nTweet: " + "https://twitter.com/" + contents.getUser().getScreenName() + "/status/" + contents.getId());
    }
}
