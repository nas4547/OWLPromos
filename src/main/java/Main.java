import twitter4j.*;
/**
 * A bot that automatically detects giveaways run by teams and tweets them out.
 * @author Nate S.
 */
public class Main {
    private static Twitter me;
    private static boolean detected = false;
    private static boolean verified = false;

    /**
     * The main method. Initializes Twitter, TwitterStream, and the StatusListener.
     *
     * @param args the command line arguments
     * @throws TwitterException
     */
    public static void main(String[] args) throws TwitterException {
        System.out.println("[INFO]: STARTING");
        me = TwitterFactory.getSingleton();
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();

        /* The handles to filter through */
        String[] handles = {"Hangzhou_Spark", "ChengduHunters", "GZCharge", "ATLReign", "ParisEternal",
                "TorontoDefiant", "Outlaws", "FLMayhem", "SeoulDynasty", "LAGladiators",
                "NYXL", "Fusion", "Spitfire", "DallasFuel", "LAValiant",
                "ShanghaiDragons", "BostonUprising", "SFShock", "washjustice", "VancouverTitans", "ostrich_toast"
        };

        /* The keywords to filter through */
        final String[] keyWords = {"giveaway", "copy", "free copy", "Overwatch Origins Edition", "for PC", "receive a",
                "Origins Edition", "tokens", "skins", "skin code"};

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
                        "\nTweet: " + "https://twitter.com/" + contents.getUser().getScreenName() + "/status/" + contents.getId());
    }
}
