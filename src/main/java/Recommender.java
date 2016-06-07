import algorithms.Cosine;
import algorithms.Pearson;
import algorithms.Similarity;
import model.UserPreference;
import utility.Utility;

import java.util.*;

/**
 * Created by Zahey Boukich on 4-6-2016.
 */
public class Recommender {
    private Map<Number, UserPreference> dataList;
    private Comparator<Map.Entry<Number, Double>> reverseOrderComparator = (x, y) -> x.getValue().compareTo(y.getValue());
    private Similarity simAlgorithm;
    private int n;
    private int k;

    public static void main(String[] args) {
        Recommender recommender = new Recommender(Constants.USERITEMDATA, Constants.SMALLDATA_DELIMITER, 6, new Cosine(), 3);
        recommender.recommend(7);
    }

    public Recommender(String dataFile, String splitBy, int k, Similarity simAlgorithm, int n) {
        this.dataList = Utility.loadData(dataFile, splitBy);
        this.simAlgorithm = simAlgorithm;
        this.n = n;
        this.k = k;
    }

    public void recommend(int targetUserId) {
        List<Map.Entry<Number, Double>> nearestNeighborsList = computeNearestNeighbors(targetUserId);
        System.out.println(" Neighbours : " + nearestNeighborsList);
        Map<Number, Map<Number, Double>> predictedList = predictRating(targetUserId, nearestNeighborsList);
        List<Map.Entry<Number, Double>> predictedItemRatingsList = new ArrayList<>();
        for (Map<Number, Double> map : predictedList.values()) {
            for (Map.Entry entry : map.entrySet()) {
                predictedItemRatingsList.add(entry);
            }
        }
        Collections.sort(predictedItemRatingsList, reverseOrderComparator);
        Collections.reverse(predictedItemRatingsList);

        Object[] item = predictedItemRatingsList.stream().map(Map.Entry::getKey).limit(n).toArray();
        for (Object itemId : item) {
            System.out.printf("Recommanded for you %s\n", itemId);
        }
    }

    public List<Map.Entry<Number, Double>> computeNearestNeighbors(Number targetUserId) {
        Map<Number, Double> distances = new HashMap<>();
        List<Map.Entry<Number, Double>> nearestNeighborsList = new ArrayList<>();
        Set<Number> users = dataList.keySet();
        for (Number user : users) {
            if (user != targetUserId) {
                distances.put(user, simAlgorithm.calculate(dataList.get(user), dataList.get(targetUserId)));
            }
        }
        nearestNeighborsList.addAll(distances.entrySet());
        Collections.sort(nearestNeighborsList, reverseOrderComparator);
        Collections.reverse(nearestNeighborsList);
        return nearestNeighborsList.subList(0, k);
    }

    public Map<Number, Map<Number, Double>> predictRating(int targetUserID, List<Map.Entry<Number, Double>> nearestNeighborsList) {
        Map<Number, Map<Number, Double>> recommendations = new TreeMap<>();
        Map<Number, Double> targetUserRatings = dataList.get(targetUserID).getRatings();
        double totalDistance = 0.0;
        for (Map.Entry<Number, Double> distance : nearestNeighborsList) {
            totalDistance += distance.getValue();
        }
        for (Map.Entry<Number, Double> distance : nearestNeighborsList) {
            double weight = distance.getValue() / totalDistance;
            Number user = distance.getKey();
            Map<Number, Double> neighborRatings = dataList.get(user).getRatings();

            for (Number item : neighborRatings.keySet()) {
                if (!targetUserRatings.containsKey(item)) {
                    TreeMap<Number, Double> weightedHashMap = new TreeMap<>();
                    Double itemresult = (neighborRatings.get(item)) * weight;
                    if (!recommendations.containsKey(item)) {
                        weightedHashMap.put(item, itemresult);
                    } else {
                        Double alreadyRated = recommendations.get(item).get(item);
                        Double sum = itemresult + alreadyRated;
                        weightedHashMap.put(item, sum);
                    }
                    recommendations.put(item, weightedHashMap);
                }
            }
        }
        return recommendations;
    }
}