package com.example.msg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.msg.DatabaseModel.RestaurantModel;
import com.example.msg.DatabaseModel.RestaurantProductModel;
import com.example.msg.DatabaseModel.SubscriptionModel;
import com.example.msg.DatabaseModel.UserModel;
import com.example.msg.DatabaseModel.UserProductModel;
import com.example.msg.Domain.RestaurantApi;
import com.example.msg.Domain.SubscriptionApi;
import com.example.msg.Domain.UserApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


public class SaleUserActivity extends AppCompatActivity {

    private int stuck = 10;

    private Button btn_chat;
    private Button btn_buy;
    private TextView txt_title;
    private TextView txt_category;
    private TextView txt_quantity;
    private TextView txt_quality;
    private TextView txt_expireDate;
    private TextView txt_description;
    private TextView txt_salesman;
    private TextView txt_address;
    private ImageView image_product;
    private Button btn_subscription;

    private static int current = -1;

    String r_name = "";

    private void getUserModelFromProduct(UserProductModel userProductModel) {
        UserApi.getUserById(userProductModel.user_id, new UserApi.MyCallback() {
            @Override
            public void onSuccess(UserModel userModel) {

                if(userModel.user_name != null) txt_salesman.setText("판매자: " + userModel.user_name);
                if(userModel.user_address != null) txt_address.setText("동네: " + userModel.user_address);
            }

            @Override
            public void onFail(int errorCode, Exception e) {

            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saleuser);

        txt_category = (TextView) findViewById(R.id.saleUserActivity_textView_categoryBig);
        txt_description = (TextView) findViewById(R.id.saleUserActivity_textView_description);
        txt_expireDate = (TextView) findViewById(R.id.saleUserActivity_textView_expiredDate);
        txt_quality = (TextView) findViewById(R.id.saleUserActivity_textView_quality);
        txt_quantity = (TextView) findViewById(R.id.saleUserActivity_textView_quantity);
        txt_salesman = (TextView) findViewById(R.id.saleUserActivity_textView_salesman);
        txt_title = (TextView) findViewById(R.id.saleUserActivity_textView_title);
        btn_buy = (Button) findViewById(R.id.saleUserActivity_button_buy);
        btn_chat = (Button) findViewById(R.id.saleUserActivity_button_chat);
        image_product = (ImageView) findViewById(R.id.saleUserActivity_imageView_product);
        btn_subscription = (Button) findViewById(R.id.saleUserActivity_button_subscription);
        txt_address = (TextView) findViewById(R.id.saleUserActivity_textView_address);
        Intent intent = getIntent();
        UserProductModel userProductModel = (UserProductModel)intent.getSerializableExtra("Model");
        //인탠트에서 프로덕트 모델을 받아옴.

        getUserModelFromProduct(userProductModel);


        txt_title.setText("제목 : " + userProductModel.title);
        txt_category.setText("카테고리 : " + userProductModel.categoryBig + " -> " + userProductModel.categorySmall);
        txt_quantity.setText("양 : " + userProductModel.quantity);
        txt_quality.setText("품질 : " + userProductModel.quality);
        txt_expireDate.setText("유통기한 : " + userProductModel.expiration_date);
        txt_description.setText("상세설명 : " + userProductModel.p_description);
        Glide.with(getApplicationContext()).load(userProductModel.p_imageURL).into(image_product);

       /* btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SaleUserActivity.this, PayActivity.class);
                startActivity(intent);
            }
        });*/
    }


}

