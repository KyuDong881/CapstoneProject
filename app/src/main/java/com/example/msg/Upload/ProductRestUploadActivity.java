package com.example.msg.Upload;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.msg.Api.AuthenticationApi;
import com.example.msg.Api.GuideLineApi;
import com.example.msg.Api.RestaurantApi;
import com.example.msg.Api.RestaurantProductApi;
import com.example.msg.DatabaseModel.RestaurantModel;
import com.example.msg.DatabaseModel.RestaurantProductModel;
import com.example.msg.R;
import com.example.msg.RecyclerView.QualitySelectActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class ProductRestUploadActivity extends AppCompatActivity {

    private ImageView productImage;
    private EditText title, quantity, cost, description, stock;
    private Spinner bigCategory, smallCategory;
    private TextView qualityText,txtResult;
    private Button qualityButton, submit, fast,expireDate;
    private DatePickerDialog.OnDateSetListener callbackMethod;


    private final ArrayList<String> smallCategories = new ArrayList<>();
    private  ArrayAdapter<String> smallCategoriesAdapter;
    private final RestaurantProductModel restaurantProductModel = new RestaurantProductModel();

    private int quality=-1;
    private double defaultLatitude = 0.0, defaultLongitude = 0.0;
    private Uri imageUri = null;

    private final int PICK_FROM_ALBUM =100, QUALITY_SELECT = 101;

    //?????? ?????? ??????
    private long mLastClickTime = 0;

    private void initialize() {
        productImage = (ImageView)findViewById(R.id.product_rest_imageView_product);
        title = (EditText)findViewById(R.id.product_rest_editText_title);
        quantity = (EditText)findViewById(R.id.product_rest_editText_quantity);
        expireDate = (Button)findViewById(R.id.product_rest_button_expireDate);
        cost = (EditText)findViewById(R.id.product_rest_editText_cost);
        description = (EditText)findViewById(R.id.product_rest_editText_description);
        stock = (EditText)findViewById(R.id.product_rest_editText_stock);

        bigCategory = (Spinner)findViewById(R.id.product_rest_spinner_categoryA);
        smallCategory = (Spinner)findViewById(R.id.product_rest_spinner_categoryB);

        qualityText = (TextView)findViewById(R.id.product_rest_textView_quality);

        qualityButton = (Button) findViewById(R.id.product_rest_button_quality);
        submit = (Button) findViewById(R.id.product_rest_button_submit);
        fast = (Button) findViewById(R.id.product_rest_button_fast);

        smallCategoriesAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, smallCategories);
        smallCategory.setAdapter(smallCategoriesAdapter);
        txtResult = (TextView)findViewById(R.id.product_rest_TextView_txtResult);

    }

    private void setRestaurantProductModelFromUI() {
        restaurantProductModel.title = title.getText().toString();
        restaurantProductModel.quantity = quantity.getText().toString();
        restaurantProductModel.expiration_date = expireDate.getText().toString();
        restaurantProductModel.cost = Integer.parseInt(cost.getText().toString());
        restaurantProductModel.p_description = description.getText().toString();
        restaurantProductModel.stock = Integer.parseInt(stock.getText().toString());

        restaurantProductModel.categoryBig = bigCategory.getSelectedItem().toString();
        restaurantProductModel.categorySmall = smallCategory.getSelectedItem().toString();

        restaurantProductModel.completed = -1;
        restaurantProductModel.saleDateYear=-1;
        restaurantProductModel.saleDateMonth=-1;
        restaurantProductModel.saleDateDay=-1;
        restaurantProductModel.quality=quality;



    }

    private void postRestProduct(RestaurantProductModel restaurantProductModel) {
        final String uid = AuthenticationApi.getCurrentUid();
        restaurantProductModel.res_id = uid;
        RestaurantProductApi.postProduct(restaurantProductModel, imageUri, new RestaurantProductApi.MyCallback() {
            @Override
            public void onSuccess(RestaurantProductModel restaurantProductModel) {
                Toast.makeText(getApplicationContext(), "?????? ????????? ?????????????????????.", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFail(int errorCode, Exception e) {
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_rest_upload);
        initialize();
        this.InitializeListener();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        RestaurantApi.getUserById(uid, new RestaurantApi.MyCallback() {
            @Override
            public void onSuccess(RestaurantModel restaurantModel) {
                defaultLatitude=restaurantModel.res_latitude;
                defaultLongitude=restaurantModel.res_longitude;
            }

            @Override
            public void onFail(int errorCode, Exception e) {

            }
        });
        //???????????? ?????? ??? ???????????? ????????? ????????? ??????.
        bigCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                smallCategories.clear();
                smallCategories.addAll(GuideLineApi.getSmallCategoryList((String)parent.getItemAtPosition(position)));
                smallCategoriesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);




        //???????????? ????????? ????????? ???????????? ??????
        productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        //?????? ?????? ?????? ?????? ???
        qualityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????? ?????? ?????? threshold 1???
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                //
                Intent intent = new Intent(ProductRestUploadActivity.this, QualitySelectActivity.class);
                intent.putExtra("category", smallCategory.getSelectedItem().toString());
                startActivityForResult(intent, QUALITY_SELECT);
            }
        });

        //?????? ????????? ?????? ???
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????? ?????? ?????? threshold 5???
                if (SystemClock.elapsedRealtime() - mLastClickTime < 5000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                //

                if(title.getText().toString() == null) {
                    Toast.makeText(getApplicationContext(), "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(imageUri == null) {
                    Toast.makeText(getApplicationContext(), "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(expireDate.getText().toString() == null) {
                    Toast.makeText(getApplicationContext(), "??????????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(quality == -1) {
                    Toast.makeText(getApplicationContext(), "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(cost.getText().toString() == null) {
                    Toast.makeText(getApplicationContext(), "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(description.getText().toString() == null) {
                    Toast.makeText(getApplicationContext(), "??????????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(quantity.getText().toString() == null) {
                    Toast.makeText(getApplicationContext(), "?????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(stock.getText().toString() == null) {
                    Toast.makeText(getApplicationContext(), "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getApplicationContext(), "?????? ?????? ??? ?????????.", Toast.LENGTH_SHORT).show();
                setRestaurantProductModelFromUI();
                restaurantProductModel.fast = false;
                restaurantProductModel.longitude = defaultLongitude;
                restaurantProductModel.latitude = defaultLatitude;
                postRestProduct(restaurantProductModel);
            }
        });

        fast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????? ?????? ?????? threshold 1???
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                //
                setRestaurantProductModelFromUI();
                restaurantProductModel.fast = true;
                postRestProduct(restaurantProductModel);
            }
        });

    }



    public void InitializeListener() {
        callbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                monthOfYear++;
                expireDate.setText(year + "???" + monthOfYear + "???" + dayOfMonth + "???");

            }
        };
    }




    public void OnClickHandler(View view) {
        DatePickerDialog dialog = new DatePickerDialog(this, callbackMethod, 2020, 6, 3);

        dialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {
            imageUri = data.getData();    //????????? ?????? ??????
            productImage.setImageURI(imageUri);
        } else if (requestCode == QUALITY_SELECT) {
            quality = -1;
            if (data.hasExtra("quality")) quality = data.getIntExtra("quality", -1);
            restaurantProductModel.quality = quality;
            if (restaurantProductModel.quality==1){
                qualityText.setText("???");
            }
            else if (restaurantProductModel.quality==2){
                qualityText.setText("???");
            }
            else if (restaurantProductModel.quality==3){
                qualityText.setText("???");
            }
        }
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            String provider = location.getProvider();
            defaultLatitude = location.getLongitude();
            defaultLongitude = location.getLatitude();
            double altitude = location.getAltitude();


        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

}
