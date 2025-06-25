package com.example.flowfits.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Workout implements Serializable {
    private static final long serialVersionUID = 1L;
    private String workoutId;
    private String userId;
    private Date date;
    private WorkoutType type;
    private String name;
    private int duration; // in minutes
    private int caloriesBurned;
    private String notes;
    private List<Exercise> exercises;
    private List<String> linkedGoals;
    
    // New enhanced fields
    private WorkoutIntensity intensity;
    private int restTimeSeconds; // Rest time between exercises
    private String location; // Gym, Home, Outdoor, etc.
    private boolean isCompleted;
    private Date startTime;
    private Date endTime;
    private double averageHeartRate;
    private int totalSets;
    private int totalReps;
    private double totalWeight; // Total weight lifted
    private String workoutTemplate; // Reference to a saved template

    public enum WorkoutType {
        CARDIO("Cardio"), 
        STRENGTH("Strength"), 
        FLEXIBILITY("Flexibility"), 
        SPORTS("Sports"),
        HIIT("HIIT"),
        YOGA("Yoga"),
        PILATES("Pilates"),
        CROSSFIT("CrossFit");
        
        private final String displayName;
        
        WorkoutType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum WorkoutIntensity {
        LOW("Low", 1),
        MODERATE("Moderate", 2), 
        HIGH("High", 3),
        VERY_HIGH("Very High", 4);
        
        private final String displayName;
        private final int level;
        
        WorkoutIntensity(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getLevel() {
            return level;
        }
    }

    // Empty constructor for Firestore
    public Workout() {
        this.exercises = new ArrayList<>();
        this.linkedGoals = new ArrayList<>();
        this.isCompleted = false;
        this.intensity = WorkoutIntensity.MODERATE;
    }

    public Workout(String userId, Date date, WorkoutType type, String name, int duration, int caloriesBurned) {
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.name = name;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.exercises = new ArrayList<>();
        this.linkedGoals = new ArrayList<>();
        this.isCompleted = false;
        this.intensity = WorkoutIntensity.MODERATE;
    }

    // Getters and Setters
    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public WorkoutType getType() { return type; }
    public void setType(WorkoutType type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(int caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }

    public List<String> getLinkedGoals() { return linkedGoals; }
    public void setLinkedGoals(List<String> linkedGoals) { this.linkedGoals = linkedGoals; }

    // New enhanced getters and setters
    public WorkoutIntensity getIntensity() { return intensity; }
    public void setIntensity(WorkoutIntensity intensity) { this.intensity = intensity; }

    public int getRestTimeSeconds() { return restTimeSeconds; }
    public void setRestTimeSeconds(int restTimeSeconds) { this.restTimeSeconds = restTimeSeconds; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public double getAverageHeartRate() { return averageHeartRate; }
    public void setAverageHeartRate(double averageHeartRate) { this.averageHeartRate = averageHeartRate; }

    public int getTotalSets() { return totalSets; }
    public void setTotalSets(int totalSets) { this.totalSets = totalSets; }

    public int getTotalReps() { return totalReps; }
    public void setTotalReps(int totalReps) { this.totalReps = totalReps; }

    public double getTotalWeight() { return totalWeight; }
    public void setTotalWeight(double totalWeight) { this.totalWeight = totalWeight; }

    public String getWorkoutTemplate() { return workoutTemplate; }
    public void setWorkoutTemplate(String workoutTemplate) { this.workoutTemplate = workoutTemplate; }

    // Helper methods
    public void calculateTotals() {
        if (exercises == null) return;
        
        int sets = 0;
        int reps = 0;
        double weight = 0;
        
        for (Exercise exercise : exercises) {
            if (exercise.getSets() != null) sets += exercise.getSets();
            if (exercise.getReps() != null) reps += exercise.getReps();
            if (exercise.getWeight() != null && exercise.getSets() != null) {
                weight += exercise.getWeight() * exercise.getSets();
            }
        }
        
        this.totalSets = sets;
        this.totalReps = reps;
        this.totalWeight = weight;
    }
    
    public int getCompletedExercises() {
        if (exercises == null) return 0;
        return (int) exercises.stream().filter(Exercise::isCompleted).count();
    }
    
    // Firebase requires setter even for computed properties
    public void setCompletedExercises(int completedExercises) {
        // This is a computed property, so we don't need to store it
        // Firebase ClassMapper requires this setter to exist
    }
    
    public double getCompletionPercentage() {
        if (exercises == null || exercises.isEmpty()) return 0;
        return (getCompletedExercises() * 100.0) / exercises.size();
    }
    
    // Firebase requires setter even for computed properties
    public void setCompletionPercentage(double completionPercentage) {
        // This is a computed property, so we don't need to store it
        // Firebase ClassMapper requires this setter to exist
    }

    // Enhanced Exercise class
    public static class Exercise implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private Integer sets;
        private Integer reps;
        private Double weight;
        private Double distance;
        private String unit;
        private String category; // Push, Pull, Legs, Cardio, etc.
        private String muscleGroup; // Chest, Back, Legs, etc.
        private String notes;
        private boolean isCompleted;
        private int restTimeSeconds;
        private List<ExerciseSet> exerciseSets; // Individual set tracking

        public Exercise() {
            this.exerciseSets = new ArrayList<>();
            this.isCompleted = false;
        }

        public Exercise(String name) {
            this.name = name;
            this.exerciseSets = new ArrayList<>();
            this.isCompleted = false;
        }

        // Existing getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getSets() { return sets; }
        public void setSets(Integer sets) { this.sets = sets; }

        public Integer getReps() { return reps; }
        public void setReps(Integer reps) { this.reps = reps; }

        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }

        public Double getDistance() { return distance; }
        public void setDistance(Double distance) { this.distance = distance; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        // New enhanced getters and setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getMuscleGroup() { return muscleGroup; }
        public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }

        public int getRestTimeSeconds() { return restTimeSeconds; }
        public void setRestTimeSeconds(int restTimeSeconds) { this.restTimeSeconds = restTimeSeconds; }

        public List<ExerciseSet> getExerciseSets() { return exerciseSets; }
        public void setExerciseSets(List<ExerciseSet> exerciseSets) { this.exerciseSets = exerciseSets; }
    }
    
    // New class for individual set tracking
    public static class ExerciseSet implements Serializable {
        private static final long serialVersionUID = 1L;
        private int setNumber;
        private int reps;
        private double weight;
        private boolean isCompleted;
        private String notes;
        private Date completedAt;

        public ExerciseSet() {}

        public ExerciseSet(int setNumber, int reps, double weight) {
            this.setNumber = setNumber;
            this.reps = reps;
            this.weight = weight;
            this.isCompleted = false;
        }

        // Getters and setters
        public int getSetNumber() { return setNumber; }
        public void setSetNumber(int setNumber) { this.setNumber = setNumber; }

        public int getReps() { return reps; }
        public void setReps(int reps) { this.reps = reps; }

        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }

        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { 
            this.isCompleted = completed;
            if (completed) {
                this.completedAt = new Date();
            }
        }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public Date getCompletedAt() { return completedAt; }
        public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }
    }
} 