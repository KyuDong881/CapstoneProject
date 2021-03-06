package com.example.msg.Sale;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.msg.Api.AuthenticationApi;
import com.example.msg.Api.ChatRoomApi;
import com.example.msg.Api.RestaurantApi;
import com.example.msg.Api.RestaurantProductApi;
import com.example.msg.Api.SaleApi;
import com.example.msg.Api.SubscriptionApi;
import com.example.msg.Api.UserApi;
import com.example.msg.ChatRoom.ChatRoomActivity;
import com.example.msg.DatabaseModel.ChatRoomModel;
import com.example.msg.DatabaseModel.RestaurantModel;
import com.example.msg.DatabaseModel.RestaurantProductModel;
import com.example.msg.DatabaseModel.SaleModel;
import com.example.msg.DatabaseModel.SubscriptionModel;
import com.example.msg.DatabaseModel.UserModel;
import com.example.msg.QRcode.ResQrcodeActivity;
import com.example.msg.R;
import com.example.msg.RatingActivity;
import com.example.msg.saleFragment.ProductInfoFragment;
import com.example.msg.saleFragment.ResInfoFragment;
import com.example.msg.saleFragment.ResReviewsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SaleActivity extends AppCompatActivity {

    private static final String TAG = "SaleActivity";
    private int stuck = 10;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private Button btn_chat;
    private Button btn_buy;
    private TextView txt_title;

    private TextView txt_salesman;
    private TextView txt_address;
    private TextView txt_rating;

    private ImageView image_product;
    private Button btn_subscription;
    private Button love;
    private Button share;
    private final SubscriptionModel subscriptionModel = new SubscriptionModel();
    private int state = -1;
    private Button btn_evaluate;
    private Button QRcode;
    private RatingBar rating;
    private Button btn_edit;
    private Button dummy;
    private String name;
    private Button btn_del;
    private Button chat;
    //private Button btn_buyerInfo;
    private String user_name;
    String r_sub = "";
    FirebaseUser user;


    private ProductInfoFragment productInfoFragment;
    private ResInfoFragment resInfoFragment;
    private ResReviewsFragment resReviewsFragment;

    private void processSale(final RestaurantProductModel restaurantProductModel, final int buystock) {
        restaurantProductModel.stock -= buystock;
        UserApi.getUserById(user.getUid(), new UserApi.MyCallback() {
            SaleModel saleModel = new SaleModel();

            @Override
            public void onSuccess(UserModel userModel) {
                saleModel = new SaleModel();
                saleModel.res_id = restaurantProductModel.res_id;
                saleModel.user_id = AuthenticationApi.getCurrentUid();
                saleModel.categorySmall = restaurantProductModel.categorySmall;
                saleModel.user_name = userModel.user_name;

                if (restaurantProductModel.stock > 0) {
                    restaurantProductModel.completed = -1;
                    RestaurantProductApi.updateProduct(restaurantProductModel, new RestaurantProductApi.MyCallback() {
                        @Override
                        public void onSuccess(RestaurantProductModel restaurantProductModel) {
                            RestaurantProductModel cloneModel;
                            cloneModel = restaurantProductModel;
                            cloneModel.stock = buystock;
                            cloneModel.completed = 0;
                            Log.d("test2forsale", "before postproductwithnoimage");
                            RestaurantProductApi.postProductWithNoImage(cloneModel, new RestaurantProductApi.MyCallback() {
                                @Override
                                public void onSuccess(RestaurantProductModel restaurantProductModel) {
                                    saleModel.product_id = restaurantProductModel.rproduct_id;

                                    Log.d("test2forsale", "on resproduct post finish");
                                    SaleApi.postSale(saleModel, new SaleApi.MyCallback() {
                                        @Override
                                        public void onSuccess(SaleModel saleModel) {
                                            Log.d("test2forsale", "on sale finish");
                                            finish();
                                        }

                                        @Override
                                        public void onFail(int errorCode, Exception e) {
                                            Log.d("stock", e.toString() + Integer.toString(errorCode));
                                        }
                                    });
                                }

                                @Override
                                public void onFail(int errorCode, Exception e) {

                                    Log.d("stock", e.toString() + Integer.toString(errorCode));
                                }
                            });

                        }

                        @Override
                        public void onFail(int errorCode, Exception e) {

                            Log.d("stock", e.toString() + Integer.toString(errorCode));
                        }
                    });
                } else {
                    saleModel.product_id = restaurantProductModel.rproduct_id;
                    restaurantProductModel.completed=0;
                    SaleApi.postSale(saleModel, new SaleApi.MyCallback() {
                        @Override
                        public void onSuccess(SaleModel saleModel) {
                            RestaurantProductApi.updateProduct(restaurantProductModel, new RestaurantProductApi.MyCallback() {
                                @Override
                                public void onSuccess(RestaurantProductModel restaurantProductModel) {
                                    finish();
                                }

                                @Override
                                public void onFail(int errorCode, Exception e) {
                                    Log.d(TAG, "RestaurantProductApi.updateProduct fail");
                                }
                            });
                        }

                        @Override
                        public void onFail(int errorCode, Exception e) {

                        }
                    });
                }
            }
            @Override
            public void onFail(int errorCode, Exception e) {

            }
        });
    }


    private void getSubscribeCheck(RestaurantProductModel restaurantProductModel) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("subs3","??????");

        String uid = user.getUid();
        Log.d("uid1234", uid);

        r_sub = restaurantProductModel.res_id;
        SubscriptionApi.getSubscriptionListByUserId(uid, new SubscriptionApi.MyListCallback() {
            @Override
            public void onSuccess(ArrayList<SubscriptionModel> subscriptionModelArrayList) {
                for(int i =0;i < subscriptionModelArrayList.size();i++) {
                    if((subscriptionModelArrayList.get(i).res_id).equals(r_sub))
                    {
                        Log.d("subfailnew", "if state start");
                        subscriptionModel.user_id = subscriptionModelArrayList.get(i).user_id;
                        subscriptionModel.res_id = subscriptionModelArrayList.get(i).res_id;
                        subscriptionModel.subs_id = subscriptionModelArrayList.get(i).subs_id;

                        state = 1;
                    }
                    else {
                        Log.d("subfailnew", "else state");
                    }
                    Log.d("subfailnew", "for state end");
                }

                if(state == 1) btn_subscription.setText("?????? ??????");
                else btn_subscription.setText("??????");
            }
            @Override
            public void onFail(int errorCode, Exception e) {
                Log.d("subfail", Integer.toString(errorCode));
            }
        });
    }

    private void subscribeClick(final RestaurantProductModel restaurantProductModel) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        if(state==-1) {
            subscriptionModel.res_id = restaurantProductModel.res_id;
            subscriptionModel.user_id = uid;
            state=1;
            btn_subscription.setText("?????? ??????");
            SubscriptionApi.postSubscription(subscriptionModel, new SubscriptionApi.MyCallback() {
                @Override
                public void onSuccess(SubscriptionModel subscriptionModel) {
                    Toast.makeText(getApplicationContext(), "?????? ??????!", Toast.LENGTH_LONG).show();
                    FirebaseMessaging.getInstance().subscribeToTopic(restaurantProductModel.res_id)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //String msg = getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        //  msg = getString(R.string.msg_subscribe_failed);
                                    }
                                    //Log.d(TAG, msg);
                                }
                            });
                }

                @Override
                public void onFail(int errorCode, Exception e) {
                    Log.d("subfail2", Integer.toString(errorCode));
                }
            });
        }
        else {
            SubscriptionApi.deleteSubscriptionBySubsId(subscriptionModel.subs_id, new SubscriptionApi.MyCallback() {
                @Override
                public void onSuccess(SubscriptionModel subscriptionModel) {
                    Log.d("subSuccess", "success");
                }

                @Override
                public void onFail(int errorCode, Exception e) {
                    Log.d("subfail3", Integer.toString(errorCode));
                }
            });
            state=-1;
            Log.d("subSuccess2", Integer.toString(state));
            btn_subscription.setText("??????");

            FirebaseMessaging.getInstance().unsubscribeFromTopic(restaurantProductModel.res_id);
        }
    }



    private void getResModelFromProduct(RestaurantProductModel restaurantProductModel) {
        RestaurantApi.getUserById(restaurantProductModel.res_id, new RestaurantApi.MyCallback() {
            @Override
            public void onSuccess(RestaurantModel restaurantModel) {
                if(restaurantModel.res_name != null) txt_salesman.setText(restaurantModel.res_name);
                name=restaurantModel.res_name;
                rating.setRating(restaurantModel.res_rating);
                txt_rating.setText(Float.toString(restaurantModel.res_rating));
//                if(restaurantModel.res_address != null) txt_address.setText("??????: " + restaurantModel.res_address);
            }

            @Override
            public void onFail(int errorCode, Exception e) {

            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        viewPager=findViewById(R.id.saleActivity_viewpager);
        tabLayout=findViewById(R.id.saleActivity_tablayout);

        txt_salesman = (TextView) findViewById(R.id.saleActivity_textView_salesman);
        txt_title = (TextView) findViewById(R.id.saleActivity_textView_title);
        image_product = (ImageView) findViewById(R.id.saleActivity_imageView_product);
        btn_subscription = (Button) findViewById(R.id.saleActivity_button_subscription);
        txt_address = (TextView) findViewById(R.id.saleActivity_textView_address);
        rating= (RatingBar)findViewById((R.id.saleActivity_item_ratingBar_grade));  //!!!!!!!
        txt_rating=(TextView)findViewById(R.id.saleActivity_textView_ratingText);
        share=(Button)findViewById(R.id.saleActivity_button_share);
        //txt_cost=(TextView)findViewById((R.id.saleActivity_textView_cost));
        btn_edit=(Button)findViewById(R.id.saleActivity_button_edit);

        Intent intent = getIntent();
        final RestaurantProductModel restaurantProductModel = (RestaurantProductModel)intent.getSerializableExtra("Model");
        //??????????????? ???????????? ????????? ?????????.

        btn_buy = (Button) findViewById(R.id.saleActivity_button_buy);
        btn_chat = (Button) findViewById(R.id.saleActivity_button_chat);
        btn_evaluate = (Button) findViewById(R.id.saleActivity_button_rating);
        btn_del=(Button) findViewById(R.id.saleActivity_button_del);
        QRcode = (Button) findViewById(R.id.saleActivity_button_QRcode);
        //btn_buyerInfo = (Button) findViewById(R.id.saleActivity_button_buyerInfo);

        user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();

        if(uid.equals(restaurantProductModel.res_id)) {
            btn_buy.setVisibility(View.INVISIBLE);
            btn_evaluate.setVisibility(View.INVISIBLE);
            btn_subscription.setVisibility(View.INVISIBLE);
        }

        if(uid.equals(restaurantProductModel.res_id)&&restaurantProductModel.completed==-1) {
            btn_edit.setVisibility(View.VISIBLE);
            btn_del.setVisibility(View.VISIBLE);
        }


        if(restaurantProductModel.completed==0){
            if(uid.equals(restaurantProductModel.res_id)) {
                //btn_buyerInfo.setVisibility(View.VISIBLE);
                btn_chat.setVisibility(View.VISIBLE);
                btn_evaluate.setVisibility(View.INVISIBLE);
                QRcode.setVisibility(View.INVISIBLE);

            }
            else{
                btn_evaluate.setVisibility(View.VISIBLE);
                QRcode.setVisibility(View.VISIBLE);

            }
        }

        getResModelFromProduct(restaurantProductModel);
        getSubscribeCheck(restaurantProductModel);


        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        Double lat=intent.getExtras().getDouble("mLat");
        Double lng=intent.getExtras().getDouble("mLng");


        txt_title.setText(restaurantProductModel.title);

        productInfoFragment=new ProductInfoFragment();
        resInfoFragment =new ResInfoFragment();
        resReviewsFragment = new ResReviewsFragment();

        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),0, restaurantProductModel);
        viewPagerAdapter.addFragment(productInfoFragment,"?????? ??????");
        viewPagerAdapter.addFragment(resInfoFragment,"?????? ??????");
        viewPagerAdapter.addFragment(resReviewsFragment,"??????");
        viewPager.setAdapter(viewPagerAdapter);


        String addressString = null;
        Geocoder geocoder = new Geocoder(this, Locale.KOREAN);
        try {
            List<Address> addresses = geocoder.getFromLocation(restaurantProductModel.latitude, restaurantProductModel.longitude, 10);
            for (int i=0; i<addresses.size(); i++) {
                if(addresses.get(i).getThoroughfare() != null ) {
                    txt_address.setText(addresses.get(i).getThoroughfare());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Glide.with(getApplicationContext()).load(restaurantProductModel.p_imageURL).into(image_product);



        btn_subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscribeClick(restaurantProductModel);
                Log.d("subs4","??????");
            }
        });

        btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder stockCheck = new AlertDialog.Builder(SaleActivity.this);
                stockCheck.setTitle("????????? ??????????????????");
                stockCheck.setMessage("????????? ????????????!");
                final EditText et = new EditText(SaleActivity.this);
                stockCheck.setView(et);
                stockCheck.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int buystock = Integer.parseInt(et.getText().toString());
                        if(buystock>restaurantProductModel.stock) {
                            Toast.makeText(getApplicationContext(), "???????????? ?????? ????????? ???????????????!", Toast.LENGTH_LONG).show();
                            dialogInterface.dismiss();
                        }
                        else {
                            processSale(restaurantProductModel,buystock);
                            Intent intent = new Intent(getApplicationContext(), PayActivity.class);
                            intent.putExtra("Model", restaurantProductModel);
                            intent.putExtra("stock",buystock);
                            startActivity(intent);
                            finish();
                            dialogInterface.dismiss();
                        }
                    }
                });
                stockCheck.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                stockCheck.show();
            }
        });

        btn_evaluate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RatingActivity.class);
                intent.putExtra("Model", restaurantProductModel);
                startActivity(intent);
                finish();
            }
        });

        QRcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ResQrcodeActivity.class);
                intent.putExtra("Model", restaurantProductModel);
                startActivity(intent);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                Sharing_intent.setType("text/plain");

                String Test_Message = "????????? Text";

                Sharing_intent.putExtra(Intent.EXTRA_TEXT, Test_Message);

                Intent Sharing = Intent.createChooser(Sharing_intent, "????????????");
                startActivity(Sharing);
            }
        });
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),EditSaleActivity.class);
                intent.putExtra("Models",restaurantProductModel);
                startActivity(intent);
            }
        });

        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(SaleActivity.this, ChatRoomActivity.class);

                ChatRoomApi.makeChatRoomModelById(restaurantProductModel.res_id, false, new ChatRoomApi.MyCallback() {
                    @Override
                    public void onSuccess(ChatRoomModel chatRoomModel) {
                        intent.putExtra("object", chatRoomModel);
                        startActivity(intent);
                    }

                    @Override
                    public void onFail(int errorCode, Exception e) {

                    }
                });
            }
        });
        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RestaurantProductApi.deleteProduct(restaurantProductModel.rproduct_id, new RestaurantProductApi.MyCallback() {
                    @Override
                    public void onSuccess(RestaurantProductModel restaurantProductModel) {
                        finish();
                    }

                    @Override
                    public void onFail(int errorCode, Exception e) {

                    }
                });
            }
        });

    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

//        private String res_id=null;
//        private String rproduct_id=null;
        private RestaurantProductModel restaurantProductModel;

        private List<Fragment> fragments=new ArrayList<>();
        private List<String> fragmentTitle=new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior,RestaurantProductModel restaurantProductModel) {
            super(fm, behavior);
            this.restaurantProductModel=restaurantProductModel;
        }

        public void addFragment(Fragment fragment,String title){
            fragments.add(fragment);
            fragmentTitle.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            //fragment??? ??? ????????? ??????
            Bundle bundle = new Bundle();
            //bundle.putParcelable("restaurantProductModel", (Parcelable) restaurantProductModel);
            bundle.putString("res_id",restaurantProductModel.res_id);
            bundle.putString("rproduct_id",restaurantProductModel.rproduct_id);
            bundle.putDouble("lat", restaurantProductModel.latitude);
            bundle.putDouble("long", restaurantProductModel.longitude);
            bundle.putString("name", name);
            fragments.get(position).setArguments(bundle);
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitle.get(position);
        }
    }
}

