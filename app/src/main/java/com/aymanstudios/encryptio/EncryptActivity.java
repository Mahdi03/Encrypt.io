package com.aymanstudios.encryptio;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MediaContent;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAdOptions;
//import com.google.android.gms.ads.formats.UnifiedNativeAd;
//import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncryptActivity extends AppCompatActivity {
    //Global Vars
    Toolbar toolbar;
    NestedScrollView scroll_layout;
    EditText inputTextEncrypt;
    Spinner dropdownEncryptMethod;
    Button encryptButton;
    EditText outputTextEncrypt;
    Button copyButton;
    int k = 0;
    private NativeAd myNativeAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt);
        //Setup Toolbar
        toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle(R.string.encrypt_title_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get rest of the page's elements (views)
        scroll_layout = findViewById(R.id.scroll_layout);
        inputTextEncrypt = findViewById(R.id.inputTextEncrypt);
        dropdownEncryptMethod = findViewById(R.id.encryptMethod);
        encryptButton = findViewById(R.id.encryptButton);
        outputTextEncrypt = findViewById(R.id.outputTextEncrypt);
        copyButton = findViewById(R.id.copyButton);

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        // AdMob Native Advanced Test ID: ca-app-pub-3940256099942544/2247696110
        // AdMob Native Advanced Video Test ID: ca-app-pub-3940256099942544/1044960115
        // AdMob Encrypt Ad ID: ca-app-pub-8495483038077603/1853700314
        AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-8495483038077603/1853700314")
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        //Show the actual ad
                        if (isDestroyed()) {
                            nativeAd.destroy();
                            return;
                        }
                        // You must call destroy on old ads when you are done with them,
                        // otherwise you will have a memory leak.
                        if (nativeAd != null) {
                            nativeAd.destroy();
                        }
                        myNativeAd = nativeAd;
                        //Show the actual ad
                        FrameLayout frameLayout = findViewById(R.id.menu_unified_native_ad_view);
                        NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.unified_native_ad_view, null);
                        populateNativeAdView(nativeAd, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    }
                })
        .withNativeAdOptions(adOptions)
        .withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError errorCode) {
                Toast.makeText(EncryptActivity.this, "Failed to load native ad: "
                        + errorCode, Toast.LENGTH_SHORT).show();
                Log.e("Ad error", String.valueOf(errorCode));
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());


        //Populate Spinner (dropdown)
        //Gets all of the options from the string resource
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.encrypt_method_choices, android.R.layout.simple_spinner_item);
        //Set all of the options into a dropdown list
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Apply populated dropdown to the actual one
        dropdownEncryptMethod.setAdapter(adapter);
        //When a selection is chosen from the dropdown, encrypt
        dropdownEncryptMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                encrypt();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //When the button is clicked, encrypt
        encryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                encrypt();
            }
        });
        //Copy the result to the clipboard
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("encryptedText", outputTextEncrypt.getText().toString());
                try {
                    clipboardManager.setPrimaryClip(clip);
                    Toast.makeText(EncryptActivity.this, "Successfully copied to clipboard!!!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(EncryptActivity.this, "Sorry but your text could not be copied at this time" + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    //Define the ROT13 method for later use
    public String toROT13(String input) {
        //Make it into a char array for easier looping and replacement
        char[] values = input.toCharArray();
        for (int i = 0; i < values.length; i++) {
            char letter = values[i];
            // If lowercase
            if (letter >= 'a' && letter <= 'z') {
                if (letter > 'm') {
                    letter -= 13;
                } else {
                    letter += 13;
                }
            }
            //If uppercase
            else if (letter >= 'A' && letter <= 'Z') {
                if (letter > 'M') {
                    letter -= 13;
                } else {
                    letter += 13;
                }
            }
            //All the other characters remain unchanged
            //Replace all letters
            values[i] = letter;
        }
        //compile the list of chars into a string
        return new String(values);
    }


    private void encrypt() {
        //Get value of first textbox
        String input = inputTextEncrypt.getText().toString();
        //Get value of encryptionMethod chosen
        String encryptionMethod = dropdownEncryptMethod.getSelectedItem().toString();
        //Create a mutable string object for final result
        StringBuilder output = new StringBuilder();
        //Binary
        if (encryptionMethod.equals("Binary")) {
            for (int i = 0; i < input.length(); i++) {
                output.append(Integer.toString(input.codePointAt(i), 2) + " ");
            }
        }
        //Base-64
        else if (encryptionMethod.equals("Base-64")) {
            output.append(new String(Base64.getEncoder().encode(input.getBytes())));
        }
        //Hexadecimal
        else if (encryptionMethod.equals("Hexadecimal")) {
            for (int i = 0; i < input.length(); i++) {
                output.append(Integer.toString(input.codePointAt(i), 16) + " ");
            }
        }
        //MD5
        else if (encryptionMethod.equals("MD5")) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] messageDigest = md.digest(input.getBytes());
                BigInteger no = new BigInteger(1, messageDigest);
                String hashText = no.toString(16);
                while (hashText.length() < 32) {
                    hashText = "0" + hashText;
                }
                output.append(hashText);
            } catch (NoSuchAlgorithmException e) {
                Toast.makeText(EncryptActivity.this, "Something went wrong, please try another encryption method instead", Toast.LENGTH_LONG).show();
            }
        }
        //Octal
        else if (encryptionMethod.equals("Octal")) {
            for (int i = 0; i < input.length(); i++) {
                output.append(Integer.toString(input.codePointAt(i), 8) + " ");
            }
        }
        //ROT13
        else if (encryptionMethod.equals("ROT13")) {
            output.append(toROT13(input));
        }
        //Unicode
        else if (encryptionMethod.equals("Unicode")) {
            for (int i = 0; i < input.length(); i++) {
                output.append(input.codePointAt(i) + " ");
            }
        }
        if (!encryptionMethod.equals("Select an encryption method")) {
            //Set text of output text
            outputTextEncrypt.setText(output.toString());

            //Scroll to the bottom of the screen
            //if (k != 0) {
            View lastChild = scroll_layout.getChildAt(scroll_layout.getChildCount() - 1);
            int bottom = lastChild.getBottom() + scroll_layout.getPaddingBottom();
            int scrollY = scroll_layout.getScrollY();
            int scrollHeight = scroll_layout.getHeight();
            int delta = bottom - (scrollY + scrollHeight);
            scroll_layout.smoothScrollBy(0, delta);
            //scroll_layout.smoothScrollBy(0, scroll_layout.computeVerticalScrollRange());
            //}
            /*
             * The purpose of k is simple, when the app originally loads, it sets the value of the
             * dropdown and this calls the encrypt function, so in order to prevent it from scrolling
             * onCreate, the k is originally set to 0 but after its first call it turns to 1
             * and scrolling is allowed.
             */
            //k = 1;
        }
    }

    //Google's repo code for Native ads
    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        MediaContent adMediaContent = nativeAd.getMediaContent();

        // Updates the UI to say whether or not this ad has a video asset.
        if (adMediaContent.hasVideoContent()) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            adMediaContent.getVideoController().setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.

                    super.onVideoEnd();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (myNativeAd != null) {
            myNativeAd.destroy();
        }
        super.onDestroy();
    }

}
