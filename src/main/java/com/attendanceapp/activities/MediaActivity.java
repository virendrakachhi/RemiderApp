package com.attendanceapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.attendanceapp.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MediaActivity extends Activity {

    private ImageView imgMedia;

    private Intent intent;
    private String mediaType;
    private String mediaURL;

    private ImageLoader imageLoader = null;
    private DisplayImageOptions options;

    // Declare variables
    ProgressDialog pDialog;
    VideoView videoview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        inItView();

    }

    private void inItView() {
        intent = getIntent();
        mediaType = intent.getStringExtra("MediaType");
        mediaURL = intent.getStringExtra("MediaURL");
        imgMedia = (ImageView) findViewById(R.id.img_show_image_media);
        videoview = (VideoView) findViewById(R.id.video_view);

        if (mediaType.equalsIgnoreCase("image")) {
            videoview.setVisibility(View.GONE);
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(MediaActivity.this));
            options = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.attachment3)
                    .showImageForEmptyUri(R.drawable.attachment3)
                    .cacheInMemory()
                    .cacheOnDisc()
                    .build();

            imageLoader.displayImage(mediaURL, imgMedia, options, null);
        } else {
            imgMedia.setVisibility(View.GONE);

            // Create a progressbar
            pDialog = new ProgressDialog(MediaActivity.this);
            // Set progressbar title
            pDialog.setTitle("Video");
            // Set progressbar message
            pDialog.setMessage("Buffering...");
            pDialog.setIndeterminate(false);
            //        pDialog.setCancelable(false);
            // Show progressbar
            pDialog.show();

            try {
                // Start the MediaController
                MediaController mediacontroller = new MediaController(MediaActivity.this);
                mediacontroller.setAnchorView(videoview);
                // Get the URL from String VideoURL
                Uri video = Uri.parse(mediaURL);
                videoview.setMediaController(mediacontroller);
                videoview.setVideoURI(video);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                pDialog.dismiss();
                e.printStackTrace();
            }

            try {
                videoview.requestFocus();
                videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        pDialog.dismiss();
                        videoview.start();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MediaActivity.this, "Can't play this video", Toast.LENGTH_SHORT).show();
            }

        }


    }

    public void gotoBack(View view) {
        onBackPressed();
    }

}
