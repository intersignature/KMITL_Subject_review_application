package kmitl.projectfinal.se.kmitlbackyard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AddImageReviewActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    FirebaseUser user;
    private Button addImgBtn, showMore;
    private FrameLayout frameLayout;
    private CustomTextView rating_post;
    private CustomTextView desc_post;
    private EditText post_title;
    private static final int CHOOSE_IMAGE = 101;
    private DatabaseReference mDatabase;
    String key;
    HashMap<String, Object> result = new HashMap<>();
    String subject_id;
    Uri uriUploadImage;
    String postImgLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image_review);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        addImgBtn = findViewById(R.id.add_img_btn);
        showMore = findViewById(R.id.show_more);
        frameLayout = findViewById(R.id.frameLayout);
        rating_post = findViewById(R.id.rating_post);
        desc_post = findViewById(R.id.desc_post);
        post_title= findViewById(R.id.post_title);
        subject_id = getIntent().getStringExtra("subjectSelect");


        mDatabase = FirebaseDatabase.getInstance().getReference();
        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frameLayout.setVisibility(View.VISIBLE);
                showMore.setVisibility(View.INVISIBLE);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("เขียนรีวิววิชาเรียน");
        rating_post.setText(getIntent().getStringExtra("rating"));
        desc_post.setText(getIntent().getStringExtra("comment"));

        addImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChooser();
            }
        });
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select picture profile"), CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            uriUploadImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uriUploadImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_post, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private String getCurrentTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_menu_post:
                Intent intent = new Intent(this, SubjectPostActivity.class);

                //add post database
                key = mDatabase.push().getKey();

                String score = getIntent().getStringExtra("rating");
                String description = getIntent().getStringExtra("comment");
                String title = this.post_title.getText().toString();
                String uid = user.getDisplayName();
                String timeStamp = getCurrentTime();
                String score_num = "0";

                result.put("score", score);
                result.put("description", description);
                result.put("title", title);
                result.put("uid", uid);
                result.put("subject_id", subject_id);
                result.put("timeStamp", timeStamp);
                result.put("score_num", score_num);
                 uploadImageToFirebaseStorage(key);
                //Log.i("sldfmksdkmfsdf",result+"");
                mDatabase.child("post").child(key).setValue(result);
                intent.putExtra("subjectSelect", subject_id);
                startActivity(intent);
                finish();
                break;
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }


    private void uploadImageToFirebaseStorage(final String key) {
        final StorageReference uploadImageRef =
                FirebaseStorage.getInstance().getReference("post_review_pic/"+key+".jpg");
        final Uri[] downloadUrl = new Uri[1];
        if(uriUploadImage != null){
            uploadImageRef.putFile(uriUploadImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    downloadUrl[0] = taskSnapshot.getDownloadUrl();
                    //result.put("postImgLink", downloadUrl[0] +"");
                    mDatabase.child("post").child(key).child("postImgLink").setValue(String.valueOf(downloadUrl[0]));
                    Log.i("sldfmksdkmfsdf", downloadUrl[0] +"");
                }
            });
        }
       // return downloadUrl[0]+"";
    }
}
