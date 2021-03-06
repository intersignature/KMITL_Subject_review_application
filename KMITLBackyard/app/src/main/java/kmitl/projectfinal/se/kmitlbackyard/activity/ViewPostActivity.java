package kmitl.projectfinal.se.kmitlbackyard.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kmitl.projectfinal.se.kmitlbackyard.view.CustomTextView;
import kmitl.projectfinal.se.kmitlbackyard.R;

public class ViewPostActivity extends AppCompatActivity {

    private Button addComment;
    private CustomTextView post_nickname;
    private CustomTextView post_title;
    private CustomTextView post_subject;
    private CustomTextView post_desc;
    private RatingBar post_rating;
    private CustomTextView post_date;
    private CircleImageView image_icon;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private Button edit_post, delete_post;
    private String post_id, subject_id;
    private String num_star, mNickName, mTitle, mSubject, mDesc, mDate, type, viewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        addComment = findViewById(R.id.add_comment);
        post_nickname = findViewById(R.id.post_nickname);
        post_title = findViewById(R.id.post_title);
        post_subject = findViewById(R.id.post_subject);
        post_desc = findViewById(R.id.post_desc);
        post_rating = findViewById(R.id.post_rating);
        post_date = findViewById(R.id.post_date);
        image_icon = findViewById(R.id.image_icon);
        edit_post = findViewById(R.id.edit_post);
        delete_post = findViewById(R.id.delete_post);

        subject_id = getIntent().getStringExtra("subjectSelect");

        if(user.getUid().equals(getIntent().getStringExtra("user_key"))){
            edit_post.setVisibility(View.VISIBLE);
            delete_post.setVisibility(View.VISIBLE);
        }
        Bundle bundle =getIntent().getExtras();
        if(bundle != null){
            post_id = bundle.getString("post_id");
            num_star = bundle.getString("post_rating");
            mNickName = bundle.getString("post_nickname");
            mTitle = bundle.getString("post_title");
            mSubject = bundle.getString("post_subject");
            mDesc = bundle.getString("post_desc");
            mDate = bundle.getString("post_date");
            type = bundle.getString("type");
            viewer = bundle.getString("viewer");
        }
        Map<String, Object> like = new HashMap<String, Object>();
        like.put("viewer", String.valueOf(Integer.parseInt(viewer)+1));
        mDatabase.child("view").child(post_id).updateChildren(like);

        post_nickname.setText(mNickName);
        post_title.setText(mTitle);
        post_subject.setText(mSubject);
        post_desc.setText(mDesc);
        post_rating.setRating(Float.parseFloat(num_star));
        post_date.setText(mDate);

        if(getIntent().getStringExtra("post_profile_link") != null && !getIntent().getStringExtra("post_profile_link").equals("")){
            Picasso.with(getApplicationContext()).load(getIntent().getStringExtra("post_profile_link")).fit().centerCrop().into(image_icon);
        }

        edit_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), EditPostActivity.class);

                intent.putExtra("subjectSelect", subject_id);
                intent.putExtra("post_rating", num_star);
                intent.putExtra("post_id", post_id);
                intent.putExtra("post_nickname", mNickName);
                intent.putExtra("post_title", mTitle);
                intent.putExtra("post_subject", mSubject);
                intent.putExtra("post_desc", mDesc);
                intent.putExtra("post_date", mDate);
                intent.putExtra("type", type);
                
                startActivity(intent);
            }
        });

        delete_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ViewPostActivity.this);
                builder.setMessage("คุณยืนยันที่จะลบโพสนี้หรือไม่?");
                builder.setCancelable(true);
                builder.setNegativeButton("ยืนยัน", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mDatabase.child("post").child(post_id).removeValue();
                        mDatabase.child("Likes").child(post_id).removeValue();
                        mDatabase.child("comment").child(post_id).removeValue();
                        MDToast mdToast = MDToast.makeText(getApplicationContext(), "โพสต์ถูกลบแล้ว", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS);
                        mdToast.show();
                        Intent intent;
                        if(type.equals("history")){
                            intent = new Intent(getApplicationContext(), ShowHistoryActivity.class);
                            intent.putExtra("uid", user.getDisplayName());
                            intent.putExtra("user_key", user.getUid());

                        }else if(type.equals("news")){
                            intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("type", type);
                        }else {
                            intent = new Intent(getApplicationContext(), SubjectPostActivity.class);
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("subjectSelect", subject_id);
                        startActivity(intent);
                        finish();

                    }
                });
                builder.setPositiveButton("ยกเลิก", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CommentActivity.class);
                intent.putExtra("post_id", post_id);
                startActivity(intent);
            }
        });

        post_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ShowHistoryActivity.class);
                intent.putExtra("uid", getIntent().getStringExtra("post_nickname"));
                intent.putExtra("user_key", getIntent().getStringExtra("user_key"));
                startActivity(intent);
            }
        });

        image_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ShowHistoryActivity.class);
                intent.putExtra("uid", getIntent().getStringExtra("post_nickname"));
                intent.putExtra("user_key", getIntent().getStringExtra("user_key"));
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

}
