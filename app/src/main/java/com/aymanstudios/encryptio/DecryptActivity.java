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
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.Base64;

public class DecryptActivity extends AppCompatActivity {
    //Global Vars
    Toolbar toolbar;
    NestedScrollView scroll_layout;
    EditText inputTextDecrypt;
    Spinner dropdownDecryptMethod;
    Button decryptButton;
    EditText outputTextDecrypt;
    Button copyButton;
    private NativeAd myNativeAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt);
        //Setup Toolbar
        toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle(R.string.decrypt_title_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get rest of the page's elements (views)
        scroll_layout = findViewById(R.id.scroll_layout);
        inputTextDecrypt = findViewById(R.id.inputTextDecrypt);
        dropdownDecryptMethod = findViewById(R.id.decryptMethod);
        decryptButton = findViewById(R.id.decryptButton);
        outputTextDecrypt = findViewById(R.id.outputTextDecrypt);
        copyButton = findViewById(R.id.copyButton);

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();


        // AdMob Native Advanced Test ID: ca-app-pub-3940256099942544/2247696110
        // AdMob Native Advanced Video Test ID: ca-app-pub-3940256099942544/1044960115
        // AdMob Decrypt Ad ID: ca-app-pub-8495483038077603/1060666854
        //AdMob ID Goes Here For Native Ad
        AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-8495483038077603/1060666854")
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
                    populateNativeAdView(myNativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }
            })
            .withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(LoadAdError errorCode) {
                    Toast.makeText(DecryptActivity.this, "Failed to load native ad: "
                            + errorCode, Toast.LENGTH_SHORT).show();
                }
            })
            .withNativeAdOptions(adOptions)
            .build();

        adLoader.loadAd(new AdRequest.Builder().build());


        //Populate Spinner (dropdown)
        //Gets all of the options from the string resource
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.decrypt_method_choices, android.R.layout.simple_spinner_item);
        //Set all of the options into a dropdown list
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Apply populated dropdown to the actual one
        dropdownDecryptMethod.setAdapter(adapter);
        //When a selection is chosen from the dropdown, encrypt
        dropdownDecryptMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                decrypt();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //When the button is clicked, encrypt
        decryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrypt();
            }
        });
        //Copy the result to the clipboard
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("decryptedText", outputTextDecrypt.getText().toString());
                try {
                    clipboardManager.setPrimaryClip(clip);
                    Toast.makeText(DecryptActivity.this, "Successfully copied to clipboard!!!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(DecryptActivity.this, "Sorry but your text could not be copied at this time" + e, Toast.LENGTH_LONG).show();
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


    private void decrypt() {
        //Get value of first textbox
        String input = inputTextDecrypt.getText().toString();
        //Get value of encryptionMethod chosen
        String decryptionMethod = dropdownDecryptMethod.getSelectedItem().toString();
        //Create a mutable string object for final result
        StringBuilder output = new StringBuilder();
        boolean success = true;
        try {
            //Binary
            switch (decryptionMethod) {
                case "Binary": {
                    String[] stringArr = input.split(" ");
                    for (String s : stringArr) {
                        output.append((char) Integer.parseInt(s, 2));
                    }
                    success = true;
                    break;
                }
                //Base-64
                case "Base-64":
                    output.append(new String(Base64.getDecoder().decode(input)));
                    success = true;
                    break;
                //Hexadecimal
                case "Hexadecimal": {
                    String[] stringArr = input.split(" ");
                    for (String s : stringArr) {
                        output.append((char) Integer.parseInt(s, 16));
                        success = true;
                    }
                    break;
                }
                //Octal
                case "Octal": {
                    String[] stringArr = input.split(" ");
                    for (String s : stringArr) {
                        output.append((char) Integer.parseInt(s, 8));
                    }
                    success = true;
                    break;
                }
                //ROT13
                case "ROT13":
                    output.append(toROT13(input));
                    success = true;
                    break;
                //Unicode
                case "Unicode": {
                    String[] stringArr = input.split(" ");
                    for (String s : stringArr) {
                        output.append((char) Integer.parseInt(s));
                    }
                    success = true;

                    break;
                }
            }
        } catch (Exception e) {
            Toast.makeText(DecryptActivity.this, "Try using a correct format before continuing", Toast.LENGTH_LONG).show();
            success = false;
            Log.i("error is", e.toString());
        }
        if (!decryptionMethod.equals("Select a decryption method")) {
            if (success) {
                //Set text of output text
                outputTextDecrypt.setText(output.toString());

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
    }

    //Google's repo code for Native ads
    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView(adView.findViewById(R.id.ad_media));

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
