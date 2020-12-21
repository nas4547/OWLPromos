import twitter4j.*;

import java.util.Arrays;

/**
 * A bot that automatically detects giveaways run by teams and tweets them out.
 *
 * @author Nate S.
 */
public class Main {
    private static Twitter me; /* The twitter defined in twitter4j.properties */
    private static boolean detected = false; /* A boolean to prevent duplicate occurrences of keywords in statusTexts */
    private static boolean verified = false; /* A boolean to verify that the flagged tweet is posted by a username in String[] handles */
    private static boolean published = false; /* A boolean that tracks whether a tweet was already published. */
    private static String type = "Unknown"; /* The default 'Type' of tweet */
    private static NLogger logger = new NLogger(true);

    /**
     * The main method. Initializes Twitter, TwitterStream, and the StatusListener.
     *
     * @param args the command line arguments
     * @throws TwitterException
     */
    public static void main(String[] args) throws TwitterException {
        logger.info("Running main class");
        me = TwitterFactory.getSingleton();
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();

        /* The handles to filter through */
        String[] handles = {"Hangzhou_Spark", "ChengduHunters", "GZCharge", "ATLReign", "ParisEternal",
                "TorontoDefiant", "Outlaws", "FLMayhem", "SeoulDynasty", "LAGladiators",
                "NYXL", "Fusion", "Spitfire", "DallasFuel", "LAValiant",
                "ShanghaiDragons", "BostonUprising", "SFShock", "washjustice", "VancouverTitans"
        };

        logger.info("Handles: " + Arrays.toString(handles));

        /* The keywords to filter through */
        final String[] keyWords = {"giveaway", "free copy", "Overwatch Origins Edition", "for PC",
                "Origins Edition", "tokens", "skin code", "spray code"};

        logger.info("Keywords: " + Arrays.toString(keyWords));

        /* An array to store the handles to filter through, as long userId's */
        final long[] teams = new long[handles.length];
        for (int i = 0; i < handles.length; i++) {
            User account = me.showUser(handles[i]);
            teams[i] = account.getId();
        }
        logger.info("IDs: " + Arrays.toString(teams));
        /**
         * A listener to detect status updates (new tweets)
         * @param status Information of the tweet (author, text, etc.)
         */
        StatusListener statusListener = new StatusAdapter() {
            @Override
            public void onStatus(Status status) {
                logger.info("A tweet from " + status.getUser().getScreenName() + " was flagged.");
                String statusText = status.getText();
                for (String keyword : keyWords) {
                    if (statusText.toLowerCase().contains(keyword) && !detected) { /* Prevents duplicates and case sensitivity */
                        for (long id : teams) {                                    /* Prevents replies from being flagged */
                            if (id == status.getUser().getId())
                                verified = true;
                        }
                        logger.info("Verified? " + verified);
                        if (verified) {
                            detected = true;
                            type = findType(status);
                            logger.info("This tweet is a promotion");
                            logger.info("Type found: " + type);
                            logger.info("Attempting to publish tweet...");
                            try {
                                published = true;
                                publishTweet(status);
                            } catch (TwitterException e) {
                                logger.warn("Exception occurred when attempting to publish tweet: ", e);
                            }
                        }
                    }
                } if(!published) {
                    logger.info("This tweet was either a reply or a non-promotion\n");
                }
                if (status.getURLEntities().length != 0 && !published) {
                    if (status.getURLEntities()[0].getExpandedURL().contains("spraycode")
                            || status.getURLEntities()[0].getExpandedURL().contains("owgamecodes")) {
                        logger.info("Attempting to publish tweet...");
                        try {
                            publishTweet(status);
                            published = true;
                        } catch (TwitterException e) {
                            logger.warn("Exception found when publishing with URL: ", e);
                        }
                    }
                }
                detected = false;
                verified = false;
                published = false;
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
        if (statusText.contains("giveaway") || statusText.contains("will win") || statusText.contains("give away"))
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
        if (contents.getURLEntities().length != 0) {
            url = contents.getURLEntities()[0].getURL();
        } else
            url = "N/A";
        me.updateStatus(
                "The " + contents.getUser().getName() + " are running a promotion." +
                        "\nType: " + type +
                        "\nLink: " + url +
                        "\nTweet: " + "https://twitter.com/" + contents.getUser().getScreenName() + "/status/" + contents.getId());
        logger.info("Tweet published to account Id: " + me.getId());
    }
}
