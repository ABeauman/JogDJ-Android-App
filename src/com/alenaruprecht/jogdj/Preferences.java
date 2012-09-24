package com.alenaruprecht.jogdj;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

// This class is used to store user preferences.
public class Preferences
{
    public final static String PREFERENCES_NAME = "jogdj_preferences";
    
    public enum SpeedCalculationMethod 
    {
        GPS,
        Pedometer
    }
    
    public enum DistanceUnit 
    {
        Km,
        Miles
    }
    
    public enum DistanceTrigger 
    {
        None,
        Quarter,
        Half,
        ThreeQuarter,
        One
    }
    
    public enum TimeTrigger 
    {
        None,
        One,
        Two,
        Three,
        Four,
        Five
    }
    
    //--------------------------------------------------    
    // Speed calculation method.
    public static SpeedCalculationMethod getSpeedCalculationMethod(Context context) 
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, 0);
        int value = prefs.getInt(context.getString(R.string.pref_key_speed_calculation_method), SpeedCalculationMethod.GPS.ordinal());
        return SpeedCalculationMethod.class.getEnumConstants()[value];
    }
 
    public static void setSpeedCalculationMethod(Context context, SpeedCalculationMethod newValue) 
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, 0);
        
        Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(context.getString(R.string.pref_key_speed_calculation_method), newValue.ordinal());
        prefsEditor.commit();
    }
    
    //--------------------------------------------------    
    // Distance unit.
    public static DistanceUnit getDistanceUnit(Context context) 
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, 0);
        int value = prefs.getInt(context.getString(R.string.pref_key_distance_unit), DistanceUnit.Km.ordinal());
        return DistanceUnit.class.getEnumConstants()[value];
    }
 
    public static void setDistanceUnit(Context context, DistanceUnit newValue) 
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, 0);
        
        Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(context.getString(R.string.pref_key_distance_unit), newValue.ordinal());
        prefsEditor.commit();
    }
    
    //--------------------------------------------------    
    // Distance trigger.
    public static float getDistanceTrigger(Context context) 
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, 0);
        float value = prefs.getFloat(context.getString(R.string.pref_key_distance_trigger), 0.0f);
        return value;
    }
 
    public static void setDistanceTrigger(Context context, float newValue) 
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, 0);
        
        Editor prefsEditor = prefs.edit();
        prefsEditor.putFloat(context.getString(R.string.pref_key_distance_trigger), newValue);
        prefsEditor.commit();
    }
    
    //--------------------------------------------------    
    // Time trigger.
    public static int getTimeTrigger(Context context) 
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, 0);
        int value = prefs.getInt(context.getString(R.string.pref_key_time_trigger), 0);
        return value;
    }
 
    public static void setTimeTrigger(Context context, int newValue) 
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, 0);
        
        Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(context.getString(R.string.pref_key_time_trigger), newValue);
        prefsEditor.commit();
    }
}