package com.example.msg.UserFragment;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.msg.Api.AuthenticationApi;
import com.example.msg.Api.RestaurantApi;
import com.example.msg.Api.StatisticsApi;
import com.example.msg.Api.UserApi;
import com.example.msg.Filter.FilterModel;
import com.example.msg.DatabaseModel.FoodModel;
import com.example.msg.DatabaseModel.RestaurantProductModel;
import com.example.msg.DatabaseModel.UserModel;
import com.example.msg.DatabaseModel.UserProductModel;
import com.example.msg.Api.RestaurantProductApi;
import com.example.msg.Api.UserProductApi;
import com.example.msg.Filter.FilterSelectActivity;
import com.example.msg.R;
import com.example.msg.RecyclerView.ResProductsAdapter;
import com.example.msg.RecyclerView.UserProductsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class HomeFragment extends Fragment  {
    private View view;
    private static final String TAG = "HomeFragment";

    //???????????? ??????
    private final int FILTER_CODE = 100;

    //???????????? ?????????
    private ArrayList spinnerList = new ArrayList<>();
    private Spinner spinner;
    private ArrayAdapter spinnerAdapter;
    private ImageButton searchButton;
    private EditText searchText;
    private Button dummy;

    private Button address1;
    private Button address2;
    private double altitude;
    private TextView address;
    private ImageButton filter;

    private SwipeRefreshLayout refreshLayout;

    //HomeFragment?????? ???????????? ?????????
    private double defaultLongitude = 0;
    private double defaultLatitude = 0;
    private int range = 10000000;
    private String dong="";
    static int state = -1;


    //?????????????????? ?????? ??????
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;


    //?????????????????? RestaurantProducts ?????? ??????
    private RecyclerView.Adapter resAdapter;
    private  ArrayList<RestaurantProductModel> filteredResModels = new ArrayList<RestaurantProductModel>();
    private final ArrayList<RestaurantProductModel> restaurantProductModels = new ArrayList<RestaurantProductModel>();

    //?????????????????? UserProducts ?????? ??????.
    private RecyclerView.Adapter userAdapter;
    private final ArrayList<UserProductModel> userProductModelArrayList = new ArrayList<UserProductModel>();
    private ArrayList<UserProductModel> filteredUserModels = new ArrayList<UserProductModel>();

    //?????????????????? ????????? ???????????? ??????.
    private boolean isShowingUserProduct = true;



    private void getAddress(String uid){
        UserApi.getUserById(uid, new UserApi.MyCallback() {
            @Override
            public void onSuccess(UserModel userModel) {
                defaultLatitude=userModel.latitude;
                defaultLongitude=userModel.longitude;
                //Toast.makeText(getActivity(), defaultLatitude+" "+defaultLongitude, Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFail(int errorCode, Exception e) {

            }
        });
    }
    //????????????

    private void initializeLayout(final Context context) {
        //?????????????????? ?????? ?????????
        //layoutManager = new LinearLayoutManager(getActivity()); //fragment??? ??? LinearLayoutManager ??????
        recyclerView = view.findViewById(R.id.home_recyclerView);
        recyclerView.setHasFixedSize(true); //?????????????????? ???????????? ??????
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        resAdapter = new ResProductsAdapter(filteredResModels, context);
        userAdapter = new UserProductsAdapter(filteredUserModels, context);





        //?????? ???????????? ?????? ?????????.
        spinnerList.add("?????? ??? ??????");
        spinnerList.add("?????? ??? ??????");
        spinnerList.add("?????? ??? ??????");


        searchButton = (ImageButton) view.findViewById(R.id.home_button_search);
        searchText = (EditText) view.findViewById(R.id.home_editText_search);
        filter = (ImageButton)view.findViewById(R.id.home_btn_sort);
        dummy = (Button) view.findViewById(R.id.home_button_dummy);
        address1=(Button)view.findViewById(R.id.home_button_address1);
        address2=(Button)view.findViewById(R.id.home_button_address2);
        address=(TextView)view.findViewById(R.id.home_TextView_dong);
        refreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.home_swipeLayout);
        if(isShowingUserProduct) refreshItemOfUserProducts();
        else refreshItemOfResProducts();

        refreshLayout.setRefreshing(false);
    }
    //?????? ????????????????????? ???????????? ???????????? ?????? ????????? ?????? ???????????????.

    private void refreshItemOfUserProducts() {
        final String myId = AuthenticationApi.getCurrentUid();
        UserProductApi.getProductList(defaultLatitude, defaultLongitude, range, new UserProductApi.MyListCallback() {
            @Override
            public void onSuccess(ArrayList<UserProductModel> userProductModels) {
                Log.d("refreshItem", String.format("upms: %d", userProductModels.size()));
                recyclerView.setAdapter(userAdapter);
                userProductModelArrayList.clear();
                userProductModelArrayList.addAll(UserProductApi.filterMyModels(userProductModels, myId));
                filteredUserModels.clear();
                filteredUserModels.addAll(userProductModelArrayList);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(int errorCode, Exception e) {
                Log.d("UserProduct Test", e.toString());
            }
        });
    }
    /*
    ??????:
    ??????:
    ??????:
     */


    private void sortItemOfResProducts(String sortBy) {
        switch(sortBy) {
            case "distance":
                RestaurantProductApi.sortByDistance(restaurantProductModels, defaultLatitude, defaultLongitude);
                break;
            case "price":
                RestaurantProductApi.sortByPrice(restaurantProductModels);
                break;
            case "stock":
                RestaurantProductApi.sortByStock(restaurantProductModels);
                break;
            default:
                Log.d("HomeFragmentError", "sortBy value is not proper");
                break;
        }
        filteredResModels.clear();
        filteredResModels.addAll(restaurantProductModels);
        resAdapter.notifyDataSetChanged();

    }


    private void refreshItemOfResProducts() {
        final ArrayList<RestaurantProductModel> temps = new ArrayList<>();

        RestaurantProductApi.getProductList(defaultLatitude, defaultLongitude, range, new RestaurantProductApi.MyListCallback() {
            @Override
            public void onSuccess(ArrayList<RestaurantProductModel> restaurantModelArrayList) {
                recyclerView.setAdapter(resAdapter);

                temps.addAll(restaurantModelArrayList);
                RestaurantProductApi.sortByFast(temps);


                RestaurantProductApi.extractSubscribedModel(temps, new RestaurantProductApi.MyListCallback() {
                    @Override
                    public void onSuccess(ArrayList<RestaurantProductModel> restaurantModelArrayList) {
                        for(int i = 0; i < restaurantModelArrayList.size(); i++) {
                            restaurantModelArrayList.get(i).title += "(?????????)";
                        }
                        restaurantProductModels.clear();
                        restaurantProductModels.addAll(restaurantModelArrayList);
                        restaurantProductModels.addAll(temps);

                        filteredResModels.clear();
                        filteredResModels.addAll(restaurantProductModels);
                        resAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFail(int errorCode, Exception e) {

                    }
                });

            }

            @Override
            public void onFail(int errorCode, Exception e) {

            }
        });


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_home,container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final Context context = view.getContext();
        final LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        initializeLayout(context);

        //swipelayout?????? ??????
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initializeLayout(context);
            }
        });



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        getAddress(uid);

//    UserApi
        StatisticsApi.getMen(new StatisticsApi.MyCallback() {
            @Override
            public void onSuccess(ArrayList<Integer> sum) {
                getLocation(defaultLatitude,defaultLongitude);
            }

            @Override
            public void onFail(int errorCode, Exception e) {

            }
        });
        //????????? ??????

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isShowingUserProduct) {
                    refreshItemOfUserProducts();

                } else {
                    refreshItemOfResProducts();
                }
                Intent intent = new Intent(getActivity(), FilterSelectActivity.class);
                intent.putExtra("isShowingUserProduct", isShowingUserProduct);
                startActivityForResult(intent, FILTER_CODE);
            }
        });
        //?????? ??????

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchText.getText() != null) {
                    if(isShowingUserProduct) {
                        filteredUserModels.clear();
                        UserProductApi.keywordSend(searchText.getText().toString(), new UserProductApi.MyFilterCallback() {
                            @Override
                            public void onSuccess(FoodModel foodModel) {
                                filteredUserModels.addAll(UserProductApi.filterByKeyWord(userProductModelArrayList,foodModel));
                                filteredUserModels.addAll(UserProductApi.filterByKeyWord2(userProductModelArrayList,searchText.getText().toString()));
                                userAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onFail(int errorCode, Exception e) {

                            }
                        });
                    }
                    else {
                        filteredResModels.clear();
                        RestaurantProductApi.keywordSend(searchText.getText().toString(), new RestaurantProductApi.MyFilterCallback() {
                            @Override
                            public void onSuccess(FoodModel foodModel) {
                                filteredResModels.addAll(RestaurantProductApi.filterByKeyWord(restaurantProductModels, foodModel));
                                filteredResModels.addAll(RestaurantProductApi.filterByKeyWord2(restaurantProductModels, searchText.getText().toString()));
                                resAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onFail(int errorCode, Exception e) {

                            }
                        });
                    }

                }
            }
        });
        //????????? ??????

        //?????? ??????
        dummy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShowingUserProduct) {
                    isShowingUserProduct = false;
                    refreshItemOfResProducts();
                    dummy.setText("?????? ??????");
                }
                else {
                    isShowingUserProduct = true;
                    refreshItemOfUserProducts();
                    dummy.setText("?????? ??????");
                }
            }
        });

        address1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final String uid = user.getUid();
                UserApi.getUserById(uid, new UserApi.MyCallback() {
                    @Override
                    public void onSuccess(UserModel userModel) {

                        defaultLatitude=userModel.latitude;
                        defaultLongitude=userModel.longitude;
                        getLocation(defaultLatitude,defaultLongitude);

                    }

                    @Override
                    public void onFail(int errorCode, Exception e) {

                    }
                });

            }
        });

        address2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission( getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions( getActivity(), new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                            0 );

                }
                else{
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    String provider = location.getProvider();
                    defaultLongitude = location.getLongitude();
                    defaultLatitude = location.getLatitude();
                    altitude = location.getAltitude();

                    getLocation(defaultLatitude,defaultLongitude);
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1000,
                            1,
                            gpsLocationListener);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            1000,
                            1,
                            gpsLocationListener);
                }


            }
        });




    }


    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            String provider = location.getProvider();
            defaultLongitude = location.getLongitude();
            defaultLatitude = location.getLatitude();
            altitude = location.getAltitude();

//                txtResult.setText("???????????? : " + provider + "\n" +
//                        "?????? : " + longitude + "\n" +
//                        "?????? : " + latitude + "\n" +
//                        "??????  : " + altitude);

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    public void getLocation(double lat, double lng){
        String addressString = null;
        Geocoder geocoder = new Geocoder(getContext(), Locale.KOREAN);
        Log.d("GOS", lat+" "+lng);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 10);
            for (int i=0; i<addresses.size(); i++) {
                if(addresses.get(i).getThoroughfare() != null ) {
                    dong = addresses.get(i).getThoroughfare();
                    address.setText(dong);
                }
//                    Log.d("GOS", addresses.get(i).getThoroughfare());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FILTER_CODE && resultCode ==  getActivity().RESULT_OK) {
            FilterModel filterModel = (FilterModel)data.getSerializableExtra("Object");
            sortItemOfResProducts(filterModel.getFilterType());
            if(isShowingUserProduct) {
                filteringUserProduct(filterModel);
            } else {
                filteringResProduct(filterModel);
            }
        }
    }

   public void filteringUserProduct(FilterModel filterModel) {
        ArrayList<UserProductModel> temps = new ArrayList<>();
        temps.addAll(userProductModelArrayList);
        if(filterModel.getCategory() != null && !filterModel.getCategory().equals("????????????")) temps = UserProductApi.filterByCategory(temps, filterModel.getCategory());
        temps = UserProductApi.filterByDistance(temps, defaultLatitude, defaultLongitude, filterModel.getRange());
        temps = UserProductApi.filterByQuality(temps, filterModel.isSearchLowQuality(), filterModel.isSearchMidQuality(), filterModel.isSearchHighQuality());

        userProductModelArrayList.clear();
        userProductModelArrayList.addAll(temps);
        filteredUserModels.clear();
        filteredUserModels.addAll(temps);
        userAdapter.notifyDataSetChanged();
   }

   public void filteringResProduct(FilterModel filterModel) {

        ArrayList<RestaurantProductModel> temps = new ArrayList<>();
        temps.addAll(restaurantProductModels);

        if(filterModel.getCategory() != null && !filterModel.getCategory().equals("????????????")) temps =  RestaurantProductApi.filterByCategory(temps, filterModel.getCategory());
        temps = RestaurantProductApi.filterByDistance(temps, defaultLatitude, defaultLongitude, filterModel.getRange());
        temps = RestaurantProductApi.filterByPrice(temps, filterModel.getPrice());
        temps = RestaurantProductApi.filterByQuality(temps, filterModel.isSearchLowQuality(), filterModel.isSearchMidQuality(), filterModel.isSearchHighQuality());

        restaurantProductModels.clear();
        restaurantProductModels.addAll(temps);
        filteredResModels.clear();
        filteredResModels.addAll(temps);
        resAdapter.notifyDataSetChanged();

   }

}
