package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.util.ArrayList;
import java.util.List;

public class ImageReqActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnLoadImages;
    private ImagePagerAdapter adapter;

    // List of image URLs
    private static final List<String> IMAGE_URLS = new ArrayList<String>() {{
        add("https://media.discordapp.net/attachments/817441321178234900/1354292353451426003/DFi71UH.JPG?ex=67eb5a0a&is=67ea088a&hm=74679e48c1aabbdc2c5e92ecf01b2970fe3e9657b14a96166484badaeb92e9ec&=&format=webp&width=1556&height=2335");
        add("https://media.discordapp.net/attachments/817441321178234900/1356158684043935784/uEWUXkL.JPG?ex=67eb8cb2&is=67ea3b32&hm=416ccb629a1211bdae08d10b85c13714cc2f6e5014ddc70b6b7b35075e6fabcc&=&format=webp&width=1462&height=1828");
        add("https://media.discordapp.net/attachments/817441321178234900/1356158685642231921/EGQyFwu.JPG?ex=67eb8cb2&is=67ea3b32&hm=34997649752eb5d0f9a2ebae71d402b37a7a59d820e168e152f90be0012270f1&=&format=webp&width=1462&height=2194");
        add("https://media.discordapp.net/attachments/817441321178234900/1356158687361892473/HK7vlfz.JPG?ex=67eb8cb3&is=67ea3b33&hm=c0aaa82c777a76d4d62f4530729f29be1b1560d61d2cdb0fab144cee1aece28e&=&format=webp&width=1462&height=2194");
        add("https://media.discordapp.net/attachments/817441321178234900/1356158689299398696/ixSFaCh.JPG?ex=67eb8cb3&is=67ea3b33&hm=0070674e4c95aa63afcabba9ee4cef1cee7de5c814a8180c60522acd5136f4b5&=&format=webp&width=1462&height=2194");
        add("https://media.discordapp.net/attachments/817441321178234900/1356158690809614470/tNeHwg4.JPG?ex=67eb8cb4&is=67ea3b34&hm=a5d4f0978f67eb279a396465d30fa8987dca128972913e304001490edf5fc247&=&format=webp&width=1462&height=975");

    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_req);

        // Initialize ViewPager2 instead of ImageView
        viewPager = findViewById(R.id.viewPager);
        btnLoadImages = findViewById(R.id.btnImageReq);

        // Setup adapter
        adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);

        btnLoadImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAllImages();
            }
        });
    }

    /**
     * Load all images in the list
     */
    private void loadAllImages() {
        for (int i = 0; i < IMAGE_URLS.size(); i++) {
            final int position = i;
            makeImageRequest(IMAGE_URLS.get(i), position);
        }
    }

    /**
     * Making image request for specific URL and position
     */
    private void makeImageRequest(String url, final int position) {
        ImageRequest imageRequest = new ImageRequest(
                url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        // Set the bitmap in adapter
                        adapter.setBitmapAtPosition(position, response);
                    }
                },
                0, // Width, set to 0 to get the original width
                0, // Height, set to 0 to get the original height
                ImageView.ScaleType.FIT_XY, // ScaleType
                Bitmap.Config.RGB_565, // Bitmap config
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors here
                        Log.e("Volley Error", "Error loading image at position " + position + ": " + error.toString());
                    }
                }
        );

        // Adding request to request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(imageRequest);
    }

    /**
     * Adapter class for ViewPager2
     */
    private class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
        private List<Bitmap> bitmaps;

        public ImagePagerAdapter() {
            this.bitmaps = new ArrayList<>();
            // Initialize with null bitmaps
            for (int i = 0; i < IMAGE_URLS.size(); i++) {
                bitmaps.add(null);
            }
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_layout, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            Bitmap bitmap = bitmaps.get(position);
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            } else {
                // Set a placeholder image or loading indicator
                holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        @Override
        public int getItemCount() {
            return bitmaps.size();
        }

        public void setBitmapAtPosition(int position, Bitmap bitmap) {
            if (position >= 0 && position < bitmaps.size()) {
                bitmaps.set(position, bitmap);
                notifyItemChanged(position);
            }
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imglt);
            }
        }
    }
}