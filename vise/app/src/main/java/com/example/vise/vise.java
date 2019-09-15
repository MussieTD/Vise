package com.example.vise;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Mussie on 12/2/2017.
 */

public class vise extends Fragment {

    public static MbtaScavenger newInstance() {
        return new MbtaScavenger();
    }

    private static final String GROCERY_LIST = "GROCERY_LIST";
    private EditText grocery_list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.front_page_fragment, container, false);
       // setting up view
       Button gloatBtn = v.findViewById(R.id.find_path_btn);
        grocery_list = v.findViewById(R.id.gricery_list_et);
        //TODO:: pass the grocery list
        // goes to static map image, maybe actual map in the future
        gloatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Mbta_map_fragment llf = new Mbta_map_fragment();
                ft.replace(R.id.fragment_container, llf);
                ft.commit();

            }
        });

//        // starts StationFoundActivity
//        gloatBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent locateIntent = new Intent(getActivity(), StationFoundActivity.class);
//                startActivity(locateIntent);
//            }
//        });
        return v;
    }


    public static String getStoredQuery_grocery_list(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(GROCERY_LIST, ""); // default preference is Km

    }

    public static void setStoredQuery_grocery_list(Context context, String grocery_list) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit() // after here you add many changes and then apply
                .putString(GROCERY_LIST, grocery_list)
                .apply();
        Log.i("pref manager", "setting query " + grocery_list);
    }

}
