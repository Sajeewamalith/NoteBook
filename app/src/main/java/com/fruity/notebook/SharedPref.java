package com.fruity.notebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

// Dark Mode
public class SharedPref {


      SharedPreferences mySharedPref ;
      public SharedPref(Context context) {
            mySharedPref = context.getSharedPreferences("filename", Context.MODE_PRIVATE);
      }
      // this method will save the nightMode State : True or False
      @SuppressLint("ApplySharedPref")
      public void setNightModeState(Boolean state) {
            SharedPreferences.Editor editor = mySharedPref.edit();
            editor.putBoolean("NightMode",state);
            editor.commit();
      }
      // this method will load the Night Mode State
      public Boolean loadNightModeState (){
            return mySharedPref.getBoolean("NightMode",false);
      }


}
