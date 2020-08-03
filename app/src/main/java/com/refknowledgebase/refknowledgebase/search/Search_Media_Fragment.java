package com.refknowledgebase.refknowledgebase.search;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.refknowledgebase.refknowledgebase.R;
import com.refknowledgebase.refknowledgebase.adapter.SearchMediaAdapter;
import com.refknowledgebase.refknowledgebase.buffer.mBuffer;
import com.refknowledgebase.refknowledgebase.model.Search_Media_Model;
import com.refknowledgebase.refknowledgebase.model.Search_Media_entities_Model;
import com.refknowledgebase.refknowledgebase.myinterface.HomeContentClickListner;
import com.refknowledgebase.refknowledgebase.myinterface.SearchClickListner;
import com.refknowledgebase.refknowledgebase.utils.Constant;
import com.refknowledgebase.refknowledgebase.utils.Methods;
import com.refknowledgebase.refknowledgebase.utils.PaginationScrollListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.refknowledgebase.refknowledgebase.utils.Constant.countries;

public class Search_Media_Fragment  extends Fragment implements SearchClickListner {

    RecyclerView rv_search_media;
    LinearLayoutManager layoutManager;
    Search_Media_Model search_media_model;
    SearchMediaAdapter searchMediaAdapter;
    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    FrameLayout fl_search_media;
    private int LASTPAGE;
    TextView tv_1;
    TextView tv_cari;
    Context mContext;
    LinearLayout ly_search_direcotry_country;
    TextView tv_result_count, tv_search_directory_country;
    int CountryId = 0;
    ImageView img_search;
    boolean is_country = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_search_media, container, false);


        tv_1 = root.findViewById(R.id.tv_1);
        tv_cari = root.findViewById(R.id.tv_cari);
        mContext = getContext();

        tv_search_directory_country = root.findViewById(R.id.tv_search_directory_country);
        tv_search_directory_country.setText(getString("MAPCOUNTRY").replace(" ", ""));

        ly_search_direcotry_country = root.findViewById(R.id.ly_search_direcotry_country);
        ly_search_direcotry_country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select Country");
                builder.setItems(countries, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tv_search_directory_country.setText(countries[which]);
                        switch (countries[which]){
                            case "Libya":
                                CountryId = 10;
                                break;
                            case "Egypt":
                                CountryId = 1;
                                break;
                            case "Sudan":
                                CountryId = 4;
                                break;
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        img_search = root.findViewById(R.id.img_search);
        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                tv_cari.setText(mBuffer.Search_key);
                searchMediaAdapter.clear();
                loadPage();
            }
        });

        return root;
    }
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv_cari.setText(mBuffer.Search_key);

        rv_search_media = view.findViewById(R.id.rv_search_media);
        fl_search_media = view.findViewById(R.id.fl_search_media);

        HomeContentClickListner homeContentClickListner = new HomeContentClickListner() {
            @Override
            public void Home_Content_ClickListner(View v, int position) {
                final Dialog dialog = new Dialog(requireContext());
                dialog.setContentView(R.layout.show_image);
                ImageView image = (ImageView) dialog.findViewById(R.id.img_media);
                Picasso.with(getContext()).load(Uri.parse(mBuffer.selected_media_id)).into(image);
                Button dialogButton = (Button) dialog.findViewById(R.id.btn_close_img);

                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        };
        searchMediaAdapter = new SearchMediaAdapter(getContext(), homeContentClickListner);
        rv_search_media.setAdapter(searchMediaAdapter);

        layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rv_search_media.setLayoutManager(layoutManager);
        rv_search_media.setItemAnimator(new DefaultItemAnimator());


        rv_search_media.addOnScrollListener(new PaginationScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;
                loadPage();
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        Methods.showProgress(getContext());
        loadPage();
    }

    private void loadPage() {

        String url = null;
        if (is_country){
            url = Constant.URL+"/api/media/es-search?keywords="+mBuffer.Search_key+"&countries[]="+CountryId+"&lang=English";
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
            url = Constant.URL+"/api/media/es-search?keywords="+mBuffer.Search_key+"&countries[]="+Setting_Country+"&lang=English";
        }

        RequestQueue queue = Volley.newRequestQueue(mContext);
//        String url = "https://api.project-info.gq/api/media/es-search?page=1&per_page=5&keywords="+mBuffer.Search_key+"&lang=English";
        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Methods.closeProgress();
                Gson gson = new Gson();
                search_media_model = gson.fromJson(response, Search_Media_Model.class);
                List<Search_Media_entities_Model> results = search_media_model.getEntities();
                searchMediaAdapter.addAll(results);

                tv_1.setText("SHOWING "+search_media_model.getTotal()+" RESULTS FOR ");
                LASTPAGE = search_media_model.getLast_page();

                if (currentPage >= LASTPAGE){
                    isLoading = true;
                }else {
                    isLoading = false;
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
                Map<String,String> params = new HashMap<String, String>();

                return params;
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

    public String getString(String key) {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constant.PREF, MODE_PRIVATE);
        String  selected =  mSharedPreferences.getString(key, "");
        return selected;
    }

    @Override
    public void myAction(String search) {
    }
}