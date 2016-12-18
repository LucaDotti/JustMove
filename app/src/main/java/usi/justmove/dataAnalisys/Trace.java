package usi.justmove.dataAnalisys;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import usi.justmove.utils.MoveActivity;

import static android.R.transition.move;

/**
 * Created by usi on 11/12/16.
 */
public class Trace {
    private List<List<Long>> times;
    private List<List<LatLng>> path;
    private List<Long> subPathTimes;
    private List<Integer> speeds;
    private double avg;
    private int max;
    private double deviation;
    private double weightedDeviation;
    private double weightedAvg;
    private int median;
    private List<Double> timeWeights;
    private List<MoveActivity> activities;

    public Trace() {
        times = new ArrayList<>();
        speeds = new ArrayList<>();
        path = new ArrayList<>();
        subPathTimes = new ArrayList<>();
        activities = new ArrayList<>();
        avg = -1;
        deviation = -1;
        timeWeights = new ArrayList<>();
        weightedDeviation = -1;
        weightedDeviation = -1;
        weightedAvg = -1;
        max = -1;
    }

    public void addSubPath(List<LatLng> subPathPoints, List<Long> subPathT, int speed) {
        path.add(subPathPoints);
        times.add(subPathT);
        speeds.add(speed);

        if(speed > max) {
            max = speed;
        }
        subPathTimes.add((subPathT.get(subPathT.size()-1) - subPathT.get(0))/1000);
    }

    public List<MoveActivity> getActivitiesPath() {
        if(activities.size() == 0) {
            for(Integer speed: speeds) {
                activities.add(getActivity(speed));
            }
        }
        return activities;
    }

    private MoveActivity getActivity(int speed) {
        if(speed <= 3) {
            return MoveActivity.STATIONARY;
        } else if (speed > 3 && speed <= 9) {
            return MoveActivity.WALKING;
        } else if (speed > 9 && speed <= 30) {
            return MoveActivity.BICYCLING;
        } else if (speed > 30 && speed <= 220) {
            return MoveActivity.DRIVING;
        } else {
            return MoveActivity.FLYING;
        }
    }

    public HashMap<MoveActivity, Float> getActivitiesPercentage() {
        HashMap<MoveActivity, Float> result = new HashMap<>();
        List<Long> pathsTimes = getTimeSpanVector();
        List<MoveActivity> activities = getActivitiesPath();
        result.put(MoveActivity.WALKING, 0f);
        result.put(MoveActivity.BICYCLING, 0f);
        result.put(MoveActivity.DRIVING, 0f);
        result.put(MoveActivity.FLYING, 0f);

//        int j = 0;
//        for(MoveActivity a: activities) {
//            System.out.println("AC " + a);
//
//                    j++;
//        }

        System.out.println("SIZE 1 " + pathsTimes.size());
        System.out.println("SIZE 2 " + activities.size());

        int i = 0;
        int totalTime = 0;
        for(MoveActivity ac: activities) {
            if(ac != MoveActivity.STATIONARY) {
                result.put(ac, result.get(activities.get(i)) + pathsTimes.get(i));
                System.out.println("TIME " + pathsTimes.get(i));
                totalTime += pathsTimes.get(i);

            }
            i++;
        }
//        System.out.println("TRACW " + totalTime);

        result.put(MoveActivity.WALKING, result.get(MoveActivity.WALKING)/totalTime);
        result.put(MoveActivity.BICYCLING, result.get(MoveActivity.BICYCLING)/totalTime);
        result.put(MoveActivity.DRIVING, result.get(MoveActivity.DRIVING)/totalTime);
        result.put(MoveActivity.FLYING, result.get(MoveActivity.FLYING)/totalTime);

        return result;
    }

    public List<Integer> getSpeeds() {
        return speeds;
    }

    public List<List<LatLng>> getPath() {
        return path;
    }

    public List<Long> getSubPathTimes() {
        return subPathTimes;
    }

    public double getAvg() {
        if(avg < 0) {
            for(Integer speed: speeds) {
                avg += speed;
            }
            avg = avg/speeds.size();
        }

        return avg;
    }

    public double getDeviation() {
        if(deviation < 0) {
            double avg = getAvg();

            for(Integer speed: speeds) {
                deviation += Math.pow(((double) speed) - avg, 2);
            }

            deviation = Math.sqrt(deviation/speeds.size());
        }

        return deviation;
    }

    public double getWeightedAvg() {
        if(weightedAvg < 0) {
            int i = 0;
            normalize(getTimeSpanVector());
            for(Integer speed: speeds) {
                weightedAvg += speed* timeWeights.get(i);
                i++;
            }
//            avg = avg/speeds.size();
            System.out.println("WEIGHTED AVG " + weightedAvg);
        }
        return weightedAvg;
    }

    public double getWeightedDeviation() {
        if(weightedDeviation < 0) {
            normalize(getTimeSpanVector());
            double wAvg = getWeightedAvg();

            int i = 0;
            for(Integer speed: speeds) {
                weightedDeviation += Math.pow(((double) speed* timeWeights.get(i)) - wAvg, 2);
            }

//            deviation = Math.sqrt(deviation/speeds.size());
            weightedDeviation = Math.sqrt(deviation);
            System.out.println("WEIGHTED DEVIATION " + weightedDeviation);

        }

        return weightedDeviation;

    }

    public void normalize(List<Long> timeSpans) {
        double len = 0;
        for(Long time: timeSpans) {
            len += time;
        }

        for(Long time: timeSpans) {
            timeWeights.add(time/len);
        }
    }

    private List<Long> getTimeSpanVector() {
        List<Long> timeSpanVector = new ArrayList<>();

        for(List<Long> timePath: times) {
            timeSpanVector.add(timePath.get(timePath.size()-1) - timePath.get(0));
            System.out.println((timePath.get(timePath.size()-1) - timePath.get(0))/1000);

        }

        return timeSpanVector;
    }

    public int getMedian() {
        List<Integer> tempSpeeds = new ArrayList<>();
        for(Integer speed: speeds) {
            tempSpeeds.add(speed);
        }
        Collections.sort(tempSpeeds);

        return tempSpeeds.get(tempSpeeds.size()/2);
    }

    public int getMax() {
        return max;
    }
}
