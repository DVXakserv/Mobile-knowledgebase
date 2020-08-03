package com.refknowledgebase.refknowledgebase.search;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.refknowledgebase.refknowledgebase.DashboardActivity;
import com.refknowledgebase.refknowledgebase.R;
import com.refknowledgebase.refknowledgebase.adapter.Home_Content_Adapter;
import com.refknowledgebase.refknowledgebase.adapter.SearchFAQAdapter;
import com.refknowledgebase.refknowledgebase.buffer.mBuffer;
import com.refknowledgebase.refknowledgebase.home_tab.Assistance_detail;
import com.refknowledgebase.refknowledgebase.model.Home_Content_Model;
import com.refknowledgebase.refknowledgebase.model.Home_Content_engitiesModel;
import com.refknowledgebase.refknowledgebase.model.Swipe_Tab_Model;
import com.refknowledgebase.refknowledgebase.model.Swipe_Tab_entitiesModel;
import com.refknowledgebase.refknowledgebase.myinterface.HomeContentClickListner;
import com.refknowledgebase.refknowledgebase.ui.SettingFragment;
import com.refknowledgebase.refknowledgebase.utils.Constant;
import com.refknowledgebase.refknowledgebase.utils.GeocodingLocation;
import com.refknowledgebase.refknowledgebase.utils.Methods;
import com.refknowledgebase.refknowledgebase.utils.PaginationScrollListener;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static com.parse.Parse.getApplicationContext;
import static com.refknowledgebase.refknowledgebase.utils.Constant.countries;

public class Search_Faq_Fragment extends Fragment {
    RecyclerView rv_faq_content;
    private List<Home_Content_engitiesModel> results_content;
    private Home_Content_Model homeContentModel;
    private SearchFAQAdapter homeContentAdapter;
    private boolean isLoading = false,  isLastPage = false, isLoading_content = false ;
    private static final int PAGE_START = 1;
    private int TOTAL_PAGES,  currentPage = PAGE_START, PER_PAGE = 5, LAST_PAGE, TOTAL_PAGES_content, PER_PAGE_content = 15, LAST_PAGE_content, service_category_ids = 107, currentPage_content;
    private LinearLayoutManager layoutManager_tab, layoutManager_content;
    RelativeLayout rl_search_view;
    TextView et_search_text;
    ImageView img_search;
    boolean is_country = false;
    int Country_Id = 0;
    RelativeLayout rl_full;
    TextView tv_result_search_faq;
    Fragment fragment;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search_faq, container, false);

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(root.getWindowToken(), 0);

        tv_result_search_faq = (TextView) root.findViewById(R.id.tv_result_search_faq);

        rv_faq_content = root.findViewById(R.id.rv_faq_content);
        HomeContentClickListner homeContentClickListner = new HomeContentClickListner() {
            @Override
            public void Home_Content_ClickListner(View v, int position) {
                mBuffer.SELECTED_CONTENT_id = homeContentAdapter.getItem(position).getid();

                fragment = new Assistance_detail();
                loadFragment(fragment);
            }
        };
        rl_full = (RelativeLayout) root.findViewById(R.id.rl_full);
        rl_full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        et_search_text = (TextView) root.findViewById(R.id.et_search_text);
        et_search_text.setText(getString("MAPCOUNTRY").replace(" ", ""));

        rl_search_view = (RelativeLayout) root.findViewById(R.id.rl_search_view);
        rl_search_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select Country");
                builder.setItems(countries, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        et_search_text.setText(countries[which]);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        img_search = (ImageView) root.findViewById(R.id.img_search);
        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_country = true;
                if (et_search_text.getText().toString().equals("Libya"))
                    Country_Id = 5;
                if (et_search_text.getText().toString().equals("Egypt"))
                    Country_Id = 6;
                if (et_search_text.getText().toString().equals("Sudan"))
                    Country_Id = 7;

                tv_result_search_faq.setText("Results for searched word: " + mBuffer.Search_key);
                homeContentAdapter.clear();
                loading_content();

                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        homeContentAdapter = new SearchFAQAdapter(getContext(), homeContentClickListner);
        rv_faq_content.setAdapter(homeContentAdapter);
        layoutManager_content = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_faq_content.setLayoutManager(layoutManager_content);
        rv_faq_content.setItemAnimator(new DefaultItemAnimator());
        rv_faq_content.addOnScrollListener(new PaginationScrollListener(layoutManager_content) {
            @Override
            protected void loadMoreItems() {
                currentPage_content += 1;
                isLoading_content = true;
                Methods.showProgress(getContext());
                loading_content();
                Methods.showProgress(getContext());
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES_content;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading_content;
            }
        });

        tv_result_search_faq.setText("Results for searched word: " + mBuffer.Search_key);

        loading_content();
        Methods.showProgress(getContext());
        return root;
    }
    @Override
    public void onViewCreated(@NotNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loading_content();
    }

    private void loading_content() {
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = null;
        if (is_country){
            url = Constant.URL+"/api/faq/es-search?keywords="+mBuffer.Search_key+"&countries[]="+Country_Id+"&lang=English";
        }else {
            int Setting_Country = 5;
            switch (getString("MAPCOUNTRY").replace(" ", "")){
                case "Libya":
                    Setting_Country = 5;
                    break;
                case "Egypt":
                    Setting_Country = 6;
                    break;
                case "Sudan":
                    Setting_Country = 7;
                    break;
            }
            url = Constant.URL+"/api/faq/es-search?keywords="+mBuffer.Search_key+"&countries[]="+Setting_Country+"&lang=English";
        }
        Log.e("SearchFAQURL", url);
        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e("SearchFAQresponse", response);
                Methods.closeProgress();
                Gson gson = new Gson();
                homeContentModel = gson.fromJson(response, Home_Content_Model.class);
                isLoading = false;
                results_content = homeContentModel.getEntities();
                homeContentAdapter.addAll(results_content);
                TOTAL_PAGES = homeContentModel.getLast_page();
                LAST_PAGE = homeContentModel.getLast_page();

                if (currentPage_content >= LAST_PAGE_content){
                    isLoading_content = true;
                }else {
                    isLoading_content = false;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Methods.closeProgress();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                return new HashMap<String, String>();
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Authorization", mBuffer.token_type + " " + mBuffer.oAuth_token);
                return params;
            }
        };
        queue.add(sr);
    }

    private void loadFragment(Fragment fragment){
        if (fragment != null){
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
        }
    }

    public String getString(String key) {
        SharedPreferences mSharedPreferences = requireContext().getSharedPreferences(Constant.PREF, MODE_PRIVATE);
        return mSharedPreferences.getString(key, "");
    }
}