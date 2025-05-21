package com.example.androidexample;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gallery Activity - Applet that appears when the user opens it from the home page.
 * This activity will be used to view photos posted by the user or other users, and employees, works using websockets.
 * @author Jordan Nguyen (hieu2k@iastate.edu)
 */
public class GalleryActivity extends AppCompatActivity {
    private static final String TAG = "GalleryActivity";

    // map of design names to their image URLs and descriptions
    private final Map<String, ArrayList<String>> designImagesMap = new HashMap<>();
    private final Map<String, String> designDescriptions = new HashMap<>();
    private final Map<String, String> designPriceRange = new HashMap<>();

    private FloatingActionButton backbtn;

    private ImagePagerAdapter adapter;
    private ViewPager2 viewPager;

    /**
     * Called when the activity is first created.
     *
     * Initializes the UI components and sets up any corresponding listeners for buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);

        // initialize backbtn
        backbtn = findViewById(R.id.gallery_back_btn);
        backbtn.setOnClickListener(v -> {
            finish();
        });

        // initialize data for nail designs using helper method
        initializeNailDesignData();

        // set up design card click listeners
        setupDesignCardClickListeners();
    }

    // helper method to reduce overall bulk.
    private void initializeNailDesignData() {
        // Glitter Ombré
        ArrayList<String> glitterOmbreImages = new ArrayList<String>() {{
            add("https://media.discordapp.net/attachments/817441321178234900/1358228520601522326/UIFPqw3.png?ex=67f31461&is=67f1c2e1&hm=283ebb4b88ca653e17694955501494487adc2cb29ebfc5d15e6e7b77f9d58583&=&format=webp&quality=lossless&width=551&height=367");
            add("https://media.discordapp.net/attachments/817441321178234900/1358228520874278942/Xu96KVK.png?ex=67f31462&is=67f1c2e2&hm=9316db819b39e8d5fe20a65fa9d7160a559a87bd0160d7bf7f0797bce02d5cc7&=&format=webp&quality=lossless&width=761&height=324");
        }};

        designImagesMap.put("Glitter Ombré", glitterOmbreImages);
        designDescriptions.put("Glitter Ombré", "Elegant gradient effect with a sparkling finish. Perfect for special occasions and evening events. Available in multiple color combinations.");
        designPriceRange.put("Glitter Ombré", "$40 - $55");

        // French Tips
        ArrayList<String> frenchTipsImages = new ArrayList<String>() {{
            add("https://media.discordapp.net/attachments/817441321178234900/1358228521151107193/8LZArgS.png?ex=67f31462&is=67f1c2e2&hm=73e1c3abf7a663e707b2a53c1a03c00823adad76af2f191f17f7e8bcd9684c06&=&format=webp&quality=lossless&width=733&height=324");
            add("https://media.discordapp.net/attachments/817441321178234900/1358228521389920459/dqiIA7E.png?ex=67f31462&is=67f1c2e2&hm=e50d049c7246e08dd4044aa229f82f8ebab03bed933c00102dc10ac68918f0e7&=&format=webp&quality=lossless&width=551&height=367");
        }};

        designImagesMap.put("French Tips", frenchTipsImages);
        designDescriptions.put("French Tips", "Classic elegance with a modern twist. Our French tips feature precision application with options for traditional white tips or colorful variations.");
        designPriceRange.put("French Tips", "$35 - $45");

        // Gel Extensions
        ArrayList<String> gelExtensionsImages = new ArrayList<String>() {{
            add("https://media.discordapp.net/attachments/817441321178234900/1358228521708945641/vZ78Slu.png?ex=67f31462&is=67f1c2e2&hm=fcc2769352bd0e8b0baa84a3e6175452ec6f868c515b81183542aada5ffcc517&=&format=webp&quality=lossless&width=551&height=367");
            add("https://media.discordapp.net/attachments/817441321178234900/1358228521947893980/lUiiuDJ.png?ex=67f31462&is=67f1c2e2&hm=cf14e8605d8b77e3aee11ae2fe7cec09fe3e09f9a05e97737a605ed672a25608&=&format=webp&quality=lossless&width=551&height=367");
        }};

        designImagesMap.put("Gel Extensions", gelExtensionsImages);
        designDescriptions.put("Gel Extensions", "Premium gel nail extensions that provide length and strength. Natural-looking and durable with various shape options including almond, square, and coffin.");
        designPriceRange.put("Gel Extensions", "$50 - $65");

        // Minimalist Art
        ArrayList<String> minimalistArtImages = new ArrayList<String>() {{
            add("https://media.discordapp.net/attachments/817441321178234900/1358228522237165638/JMOQSuf.png?ex=67f31462&is=67f1c2e2&hm=2caa5b4f694996211ac92b920fa9cdb1870bb3dcfdb87cd5181f4b3621f4bddc&=&format=webp&quality=lossless&width=247&height=165");
            add("https://media.discordapp.net/attachments/817441321178234900/1358228523403317489/ImRdUEX.png?ex=67f31462&is=67f1c2e2&hm=34c5f811b5022e99f1fadb82421927cb98938e05a72a992cc58afb771c8f8fe9&=&format=webp&quality=lossless&width=1462&height=2194");
        }};

        designImagesMap.put("Minimalist Art", minimalistArtImages);
        designDescriptions.put("Minimalist Art", "Clean, subtle nail art with geometric patterns and simple lines. Perfect for professional settings while maintaining style and elegance.");
        designPriceRange.put("Minimalist Art", "$40 - $50");

        // Fill remaining designs with default data
        String[] remainingDesigns = {"Marble Effect", "Floral Patterns", "Chrome Effect", "3D Embellishments"};
        for (String design : remainingDesigns) {
            ArrayList<String> defaultImages = new ArrayList<String>() {{
                add("https://media.discordapp.net/attachments/817441321178234900/1358228524246503475/Q80UhBF.png?ex=67f31462&is=67f1c2e2&hm=e5b7367c04a20f280b2094fdf0dbdbfb98a5bcea90f2ec645e4f6e14079e5e40&=&format=webp&quality=lossless&width=261&height=196");
            }};

            designImagesMap.put(design, defaultImages);

            // Set specific descriptions based on design name
            switch (design) {
                case "Marble Effect":
                    designDescriptions.put(design, "Elegant stone-inspired designs with swirling patterns that mimic natural marble. Available in various color combinations.");
                    designPriceRange.put(design, "$45 - $60");
                    break;
                case "Floral Patterns":
                    designDescriptions.put(design, "Delicate hand-painted floral motifs ranging from subtle accent flowers to full nail garden scenes. Perfect for spring and summer.");
                    designPriceRange.put(design, "$50 - $65");
                    break;
                case "Chrome Effect":
                    designDescriptions.put(design, "High-shine metallic finish that creates a mirror-like reflection. Available in silver, gold, rose gold, and holographic options.");
                    designPriceRange.put(design, "$45 - $55");
                    break;
                case "3D Embellishments":
                    designDescriptions.put(design, "Textured nail art with raised elements including crystals, pearls, charms, and other decorative pieces for a stunning dimensional look.");
                    designPriceRange.put(design, "$55 - $75");
                    break;
                default:
                    designDescriptions.put(design, design + " features unique nail art techniques with premium quality materials.");
                    designPriceRange.put(design, "$40 - $60");
            }
        }
    }

    private void setupDesignCardClickListeners() {
        // featured/main designs
        setupCardClickListener(R.id.designCard1, "Glitter Ombré");
        setupCardClickListener(R.id.designCard2, "French Tips");

        // all designs grid
        setupCardClickListener(R.id.designCard3, "Gel Extensions");
        setupCardClickListener(R.id.designCard4, "Minimalist Art");
        setupCardClickListener(R.id.designCard5, "Marble Effect");
        setupCardClickListener(R.id.designCard6, "Floral Patterns");
        setupCardClickListener(R.id.designCard7, "Chrome Effect");
        setupCardClickListener(R.id.designCard8, "3D Embellishments");

        // Adding click listeners for salonCards that exist in the XML but not referenced yet
        // These will point to our nail designs
        setupCardClickListener(R.id.designCard1, "Glitter Ombré");
        setupCardClickListener(R.id.designCard2, "French Tips");
        setupCardClickListener(R.id.designCard3, "Gel Extensions");
        setupCardClickListener(R.id.designCard4, "Minimalist Art");
        setupCardClickListener(R.id.designCard5, "Marble Effect");
        setupCardClickListener(R.id.designCard6, "Floral Patterns");
        setupCardClickListener(R.id.designCard7, "Chrome Effect");
        setupCardClickListener(R.id.designCard8, "3D Embellishments");
    }

    private void setupCardClickListener(int cardId, final String designName) {
        CardView cardView = findViewById(cardId);
        if (cardView != null) {
            cardView.setOnClickListener(v -> {
                showDesignDialog(designName);
            });
        }
    }

    private void showDesignDialog(String designName) {
        // Create dialog without using fullscreen theme
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.salon_gallery_dialog);

        // make dialog appear as overlay
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // set transparent color and background shading
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setDimAmount(0.7f); // controls background dimming (0.0f-1.0f)

        // setting up nail design information using the mapping
        TextView txtDesignName = dialog.findViewById(R.id.txtSalonName);
        TextView txtDesignPrice = dialog.findViewById(R.id.txtSalonRating);
        TextView txtDesignDescription = dialog.findViewById(R.id.txtSalonDescription);

        txtDesignName.setText(designName);
        txtDesignPrice.setText(designPriceRange.get(designName));
        txtDesignDescription.setText(designDescriptions.get(designName));

        // LOAD IMAGES
        // set up ViewPager for design images
        viewPager = dialog.findViewById(R.id.salonImageViewPager);
        adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);

        // set up indicator dots
        TabLayout dotsLayout = dialog.findViewById(R.id.salonImageDotsLayout);
        new TabLayoutMediator(dotsLayout, viewPager,
                (tab, position) -> {
                    // no text for dots
                }
        ).attach();

        // get images for this design
        ArrayList<String> imageUrls = designImagesMap.get(designName);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            loadImages(imageUrls, adapter);
        }

        // set up buttons
        Button btnViewMore = dialog.findViewById(R.id.btnViewMore);
        Button btnBookNow = dialog.findViewById(R.id.btnBookNow);

        // Rename buttons to reflect nail design context
        btnViewMore.setText("See More Styles");
        btnBookNow.setText("Book This Style");

        // set up close button (the x in the top right corner)
        ImageButton btnClose = dialog.findViewById(R.id.btnCloseDialog);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnViewMore.setOnClickListener(v -> {
            // You could implement a feature to show more styles in this category
            Toast.makeText(this, "More " + designName + " styles coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnBookNow.setOnClickListener(v -> {
            // Pass the selected design to the booking activity
            Intent intent = new Intent(this, SelectBookingTimeActivity.class);
            intent.putExtra("SELECTED_DESIGN", designName);
            startActivity(intent);
        });

        dialog.show();
    }

    // load all images from the URLs
    private void loadImages(List<String> imageUrls, ImagePagerAdapter adapter) {
        // clear any existing images
        adapter.clearBitmaps();

        // initialize bitmaps list with nulls
        for (int i = 0; i < imageUrls.size(); i++) {
            adapter.addEmptyBitmap();
        }

        // load each image
        for (int i = 0; i < imageUrls.size(); i++) {
            final int position = i;
            makeImageRequest(imageUrls.get(i), i);
        }
    }

    /**
     * Helper method to request the image from a URL and setting it at a position (first, second, last, etc.)
     * @param url the URL of the image
     * @param position the position the image should be placed at in the dialog/card image container
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

        // adding request to request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(imageRequest);
    }

    /**
     * An adapter class for RecyclerView to hold images read from a URL or cache. Holds images in an ordered manner which allows them to be swipable to view a different image.
     */
    private static class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
        private List<Bitmap> bitmaps;

        public ImagePagerAdapter() {
            this.bitmaps = new ArrayList<>();
        }

        public void clearBitmaps() {
            bitmaps.clear();
            notifyDataSetChanged();
        }

        public void addEmptyBitmap() {
            bitmaps.add(null);
            notifyItemInserted(bitmaps.size() - 1);
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
                // Set placeholder
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

        static class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imglt);
            }
        }
    }
}