package org.pratik;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Poligraph {
    Twitter twitter;
    public Poligraph(String cKey, String cKeySecret, String aToken, String aTokenSecret)
    {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(cKey)
                .setOAuthConsumerSecret(cKeySecret)
                .setOAuthAccessToken(aToken)
                .setOAuthAccessTokenSecret(aTokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
    }


    // searchWord can be user id, place etc
    // date should be in the format yyyy-MM-dd
    public ArrayList<Status> getTweetsInRange(String startDate, String endDate, String searchWord, int count) throws TwitterException {
        Query query = new Query(searchWord);
        query.setCount(count);
        query.setSince(startDate);
        query.setUntil(endDate);
        return twitterQuery(query);
    }

    public ArrayList<Status> twitterQuery(Query query) throws TwitterException {
        QueryResult result = twitter.search(query);
        return new ArrayList<>(result.getTweets());
    }

    public ArrayList<Status> getUserTweets(String user) throws TwitterException {
        ResponseList<Status> result = twitter.getUserTimeline(user);
        return new ArrayList<>(result);
    }

    // startDate and endDate should be in the format "yyyy-MM-dd"
    public ArrayList<Status> getUserTweetsInRange(String user, String startDate, String endDate) throws TwitterException, ParseException {
        Date startDt =new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
        Date endDt =new SimpleDateFormat("yyyy-MM-dd").parse(endDate);

        ArrayList<Status> statuses = new ArrayList<>();
        Date createdAt;
        for (Status status : getUserTweets(user)) {
            createdAt = status.getCreatedAt();
            if (createdAt.after(startDt) && createdAt.before(endDt)) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    public ArrayList<Status> getUserTweetsContainsWord(String user, String word) throws TwitterException {
        ArrayList<Status> statuses = new ArrayList<>();
        for (Status status : getUserTweets(user)) {
            if (status.getText().contains(word)) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    public ArrayList<Status> getUserTweetsContainsStringInRange(String user, String word, String startDate, String endDate) throws TwitterException, ParseException {
        ArrayList<Status> statuses = new ArrayList<>();
        for (Status status : getUserTweetsInRange(user,startDate,endDate)) {
            if (status.getText().contains(word)) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    public Result analyzeSentiment(Status status) {
        TwitterSentimentAnalysis sentimentAnalysis = new TwitterSentimentAnalysis();
        int sentimentValue = sentimentAnalysis.analyzeTweet(status.getText());
        return new Result(status, sentimentValue);
    }

    public ArrayList<Result> analyzeSentiment(ArrayList<Status> statuses) {
        ArrayList<Result> results = new ArrayList<>();
        for (Status status : statuses) {
            results.add(analyzeSentiment(status));

        }
        return results;
    }

    public static void main(String[] args) throws TwitterException, ParseException {

        String cKey = "FZR30B3lQbMVoNEZBspeoJKPH";
        String cKeySecret = "So12YRUjpr9qM9wfR921toN1yAmdvlJ2LkWIVt0bNuJF4a89Q6";
        String aToken = "1325292306413576194-URa206WZicJkTcCN92DaB2LDx78Pjr";
        String aTokenSecret = "1MLcfJUZxumquQtIDpbPsDWU8jdPr6OFbMurFcN9Mldzk";
        Query query = new Query("#iPhone");
        query.setCount(100);
        Poligraph p = new Poligraph(cKey, cKeySecret, aToken, aTokenSecret);
        ArrayList<Status> result = p.getUserTweetsInRange("elonmusk", "2020-11-10", "2020-11-12");
        ArrayList<Result> sentiments = p.analyzeSentiment(result);
        for (Result sentiment : sentiments)
            System.out.println("Status@\t" + sentiment.getStatus().getUser().getScreenName() + "\t:\t" + sentiment.getStatus().getText() + "\t:\t" + sentiment.getSentimentString());

        //"Status@\t" + status.getUser().getScreenName() + "\t:\t" + status.getText() + "\t:\t"+ status.getCreatedAt()
    }
}