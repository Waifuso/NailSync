package com.example.androidUI;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.R;
import com.example.androidexample.SharedPreferencesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class to handle API communications for rewards and offers using Volley
 */
public class RewardsService {
    private static final String TAG = "RewardsService";
    private static final String BASE_URL = "https://api.nailsync.com/v1/"; // Replace with your actual API base URL

    private final RequestQueue requestQueue;
    private final Context context;
    private final SharedPreferencesHelper prefsHelper;

    // Callback interfaces
    public interface UserRankCallback {
        void onSuccess(UserRank userRank);
        void onError(String errorMessage);
    }

    public interface RewardsCallback {
        void onSuccess(List<Reward> rewards);
        void onError(String errorMessage);
    }

    public interface OffersCallback {
        void onSuccess(List<Offer> offers);
        void onError(String errorMessage);
    }

    // Constructor
    public RewardsService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.prefsHelper = new SharedPreferencesHelper(context);
    }

    /**
     * Fetch user rank information from the API
     */
    public void getUserRank(long userId, UserRankCallback callback) {
        // For testing purposes, to avoid network errors during development
        if (true) { // Change to 'if (false)' for real API calls
            // Return mock data for testing
            MockDataProvider mockData = new MockDataProvider();
            UserRank userRank = mockData.getMockUserRank();
            callback.onSuccess(userRank);
            return;
        }

        String url = BASE_URL + "users/" + userId + "/rank";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String rankName = response.getString("rankName");
                            int currentPoints = response.getInt("currentPoints");
                            int nextRankThreshold = response.getInt("nextRankThreshold");
                            int pointsToNextRank = response.getInt("pointsToNextRank");

                            UserRank userRank = new UserRank(rankName, currentPoints, nextRankThreshold, pointsToNextRank);
                            callback.onSuccess(userRank);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing JSON response", e);
                            callback.onError("Error parsing server response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching user rank", error);
                        callback.onError("Network error: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Fetch available rewards from the API
     */
    public void getAvailableRewards(long userId, RewardsCallback callback) {
        // For testing purposes, to avoid network errors during development
        if (true) { // Change to 'if (false)' for real API calls
            // Return mock data for testing
            MockDataProvider mockData = new MockDataProvider();
            List<Reward> rewards = mockData.getMockRewards();
            callback.onSuccess(rewards);
            return;
        }

        String url = BASE_URL + "users/" + userId + "/rewards";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<Reward> rewards = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);

                                String title = jsonObject.getString("title");
                                String description = jsonObject.getString("description");
                                int pointsRequired = jsonObject.getInt("pointsRequired");
                                String expiryDate = jsonObject.getString("expiryDate");
                                String imageUrl = jsonObject.optString("imageUrl", null);

                                Reward reward = new Reward(title, description, pointsRequired, expiryDate);
                                reward.setImageUrl(imageUrl);
                                rewards.add(reward);
                            }

                            callback.onSuccess(rewards);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing JSON response", e);
                            callback.onError("Error parsing server response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching rewards", error);
                        callback.onError("Network error: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Fetch available offers from the API
     */
    public void getAvailableOffers(long userId, OffersCallback callback) {
        // For testing purposes, to avoid network errors during development
        if (true) { // Change to 'if (false)' for real API calls
            // Return mock data for testing
            MockDataProvider mockData = new MockDataProvider();
            List<Offer> offers = mockData.getMockOffers();
            callback.onSuccess(offers);
            return;
        }

        String url = BASE_URL + "users/" + userId + "/offers";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<Offer> offers = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);

                                String title = jsonObject.getString("title");
                                String description = jsonObject.getString("description");
                                String discountText = jsonObject.getString("discountText");
                                String tagText = jsonObject.getString("tagText");
                                String validityPeriod = jsonObject.getString("validityPeriod");
                                String imageUrl = jsonObject.optString("imageUrl", null);

                                Offer offer = new Offer(title, description, discountText, tagText, validityPeriod);
                                offer.setImageUrl(imageUrl);
                                offers.add(offer);
                            }

                            callback.onSuccess(offers);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing JSON response", e);
                            callback.onError("Error parsing server response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching offers", error);
                        callback.onError("Network error: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Redeem a reward
     */
    public void redeemReward(long userId, String rewardId, UserRankCallback callback) {
        // Implementation for redeeming a reward
        // This would typically be a POST request to your API
    }

    /**
     * Get the user's auth token from SharedPreferences
     */


    // Mock data provider for testing
    private static class MockDataProvider {
        public UserRank getMockUserRank() {
            return new UserRank("Silver", 3245, 4000, 755);
        }

        public List<Reward> getMockRewards() {
            List<Reward> rewards = new ArrayList<>();

            Log.d("RewardsService", "getMockRewards called");

            for(int i = 0; i < rewards.size(); i++)
            {
                Log.d("RewardsService", "getMockRewards: " + rewards.get(i).getImageResId());
            }

            Reward r1 = new Reward(
                    "Free Gel Polish Upgrade",
                    "Valid on any service",
                    250,
                    "May 30");
            r1.setImageResId(R.drawable.reward_gel_polish);
            //r1.setImageUrl("https://media.discordapp.net/attachments/767095350979723294/1358312752514601081/reward_gel_polish.png?ex=67f362d4&is=67f21154&hm=3725e41325e0f89b8819d7527cdb28a0908c783709e9ce5f0588fea3bb990002&=&format=webp&quality=lossless&width=1225&height=1229");
            rewards.add(r1);

            Reward r2 = new Reward(
                    "Free Nail Art Design",
                    "One accent nail",
                    350,
                    "June 15");
            r2.setImageResId(R.drawable.reward_free_nail);
            //r2.setImageUrl("https://media.discordapp.net/attachments/767095350979723294/1358312751461695528/reward_free_nail.png?ex=67f362d4&is=67f21154&hm=3a0e4285a30403f13544b06a6df060b4100c77ca7046527192fcbbfd0794ca11&=&format=webp&quality=lossless&width=1846&height=1229");
            rewards.add(r2);

            Reward r3 = new Reward(
                    "15% Off Any Service",
                    "Manicure or pedicure",
                    500,
                    "July 1");
            r3.setImageResId(R.drawable.reward_fifteen_off);
            //r3.setImageUrl("https://media.discordapp.net/attachments/767095350979723294/1358312750555729960/reward_fifteen_off.png?ex=67f362d3&is=67f21153&hm=a89f20e37e935aba690b42503e9e05c2074741550db164771da0b1767f1a0e40&=&format=webp&quality=lossless&width=1839&height=1229");
            rewards.add(r3);

            Reward r4 = new Reward(
                    "Early Bird Special",
                    "Book 3 days in advance and get 10% off",
                    200,
                    "Aug 10");
            r4.setImageResId(R.drawable.reward_earlybird);
            //r4.setImageUrl("https://media.discordapp.net/attachments/767095350979723294/1358312749943361587/reward_earlybird.png?ex=67f362d3&is=67f21153&hm=c14535fbbf44f66a770c2863f1f57e86bc90971fdb5c24f9ca550cae827159bd&=&format=webp&quality=lossless&width=1229&height=1229");
            rewards.add(r4);

            return rewards;
        }

        public List<Offer> getMockOffers() {
            List<Offer> offers = new ArrayList<>();

            Offer o1 = new Offer(
                    "Weekday Special",
                    "Valid on Wednesdays only",
                    "15% OFF",
                    "LIMITED",
                    "Apr 1 - May 31");
            o1.setImageResId(R.drawable.offer_weekday_special);
            //o1.setImageUrl("https://media.discordapp.net/attachments/767095350979723294/1358312749171474574/offer_weekday_special.png?ex=67f362d3&is=67f21153&hm=d77a3f8bc969133ef5f1c6515e54cd3fae882e648d22540ad516e9868318e8fa&=&format=webp&quality=lossless&width=1843&height=1229");
            offers.add(o1);

            Offer o2 = new Offer(
                    "First-Time Client",
                    "New customers only",
                    "20% OFF",
                    "EXCLUSIVE",
                    "Valid anytime");
            o2.setImageResId(R.drawable.stockplaceholderimage);
            o2.setImageUrl("https://media.discordapp.net/attachments/767095350979723294/1358320170745073745/raw.png?ex=67f369bd&is=67f2183d&hm=073ed5bfc21e96bb038cdf70c0160214a0c1d82278ca709fda584d520595454b&=&format=webp&quality=lossless&width=1411&height=1411");
            offers.add(o2);

            Offer o3 = new Offer(
                    "Chrome & Metallic Nails",
                    "Trending mirror finish and metallic designs",
                    "25% OFF",
                    "NEW",
                    "Apr 15 - May 30");
            o3.setImageResId(R.drawable.offer_metallic);
            //o3.setImageUrl("https://media.discordapp.net/attachments/767095350979723294/1358309954385215529/image.png?ex=67f36039&is=67f20eb9&hm=c12449997a8952254b05d519f79db231216328aa40a2ee4d562a8802e3e10b8a&=&format=webp&quality=lossless&width=1402&height=1411");
            offers.add(o3);

            Offer o4 = new Offer(
                    "Refer a Friend",
                    "When they book their first appointment",
                    "10% OFF",
                    "ONGOING",
                    "Always available");
            o4.setImageResId(R.drawable.stockplaceholderimage);
            //o4.setImageUrl("https://media.discordapp.net/attachments/767095350979723294/1358321072583479536/raw.png?ex=67f36a94&is=67f21914&hm=3f67ef65e61af9f069c9e793983cbde7808d773e96532b47d902c649eef40877&=&format=webp&quality=lossless&width=1411&height=1411");
            offers.add(o4);

            return offers;
        }

    }

    // Model classes with expanded functionality

    /**
     * UserRank class to store the user's rank information
     */
    public static class UserRank {
        private String rankName;
        private int currentPoints;
        private int nextRankThreshold;
        private int pointsToNextRank;

        public UserRank(String rankName, int currentPoints, int nextRankThreshold, int pointsToNextRank) {
            this.rankName = rankName;
            this.currentPoints = currentPoints;
            this.nextRankThreshold = nextRankThreshold;
            this.pointsToNextRank = pointsToNextRank;
        }

        public String getRankName() { return rankName; }
        public int getCurrentPoints() { return currentPoints; }
        public int getNextRankThreshold() { return nextRankThreshold; }
        public int getPointsToNextRank() { return pointsToNextRank; }

        /**
         * Calculate the progress percentage for the progress bar
         */
        public int getProgressPercentage() {
            if (nextRankThreshold <= 0) return 100;
            return (currentPoints * 100) / nextRankThreshold;
        }

        /**
         * Get the next rank name based on the current rank
         */
        public String getNextRankName() {
            switch (rankName.toLowerCase()) {
                case "bronze": return "Silver";
                case "silver": return "Platinum";
                case "platinum": return "Diamond";
                case "diamond": return "Diamond";
                default: return "Next Rank";
            }
        }
    }

    /**
     * Reward class to store reward information
     */
    public static class Reward {
        private String title;
        private String description;
        private int pointsRequired;
        private String expiryDate;
        private String imageUrl;

        private int imageResId = -1;

        public Reward(String title, String description, int pointsRequired, String expiryDate) {
            this.title = title;
            this.description = description;
            this.pointsRequired = pointsRequired;
            this.expiryDate = expiryDate;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getPointsRequired() { return pointsRequired; }
        public String getExpiryDate() { return expiryDate; }
        public String getImageUrl() { return imageUrl; }

        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        /**
         * Check if the reward is claimable based on the user's points
         */
        public boolean isClaimable(int userPoints) {
            return userPoints >= pointsRequired;
        }

        public int getImageResId()
        {
            return imageResId;
        }
        public void setImageResId(int imageResId)
        {
            this.imageResId = imageResId;
        }
    }

    /**
     * Offer class to store offer information
     */
    public static class Offer {
        private String title;
        private String description;
        private String discountText;
        private String tagText;
        private String validityPeriod;
        private String imageUrl;

        private int imageResId = -1;

        public Offer(String title, String description, String discountText, String tagText, String validityPeriod) {
            this.title = title;
            this.description = description;
            this.discountText = discountText;
            this.tagText = tagText;
            this.validityPeriod = validityPeriod;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getDiscountText() { return discountText; }
        public String getTagText() { return tagText; }
        public String getValidityPeriod() { return validityPeriod; }
        public String getImageUrl() { return imageUrl; }

        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        /**
         * Check if the offer is exclusive to a specific rank
         */
        public boolean isRankExclusive() {
            return "RANK EXCLUSIVE".equals(tagText);
        }

        public int getImageResId()
        {
            return imageResId;

        }
        public void setImageResId(int imageResId)
        {
            this.imageResId = imageResId;
        }
    }
    }