package softagi.urdecision;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import softagi.urdecision.Models.MessageModel;

public class MainActivity extends AppCompatActivity
{
    String CHANNEL_ID = "id";
    View v;
    Uri uri;
    String lan,gender,rate;
    LinearLayout linearLayout,face_lin;
    int counter = 0;

    TextView msg_body;
    Button next,save,back;
    CardView c1,c2,cback;
    ImageView imageView;
    FloatingActionButton more;

    List<Integer> imgs;
    List<MessageModel> messageModels;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog;

    InterstitialAd savead,sharead;
    InterstitialAd nextadd;
    AlertDialog alertDialog;
    String msg = null,yes = null,no = null;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        lan = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("lan", "arabic");
        gender = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("gen", "male");
        rate = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("rate", "yes");

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Permission is not granted
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, Manifest.permission.ACCESS_NOTIFICATION_POLICY}, 100);
        }

        savead = new InterstitialAd(MainActivity.this);
        savead.setAdUnitId("ca-app-pub-2369748974411795/1455286392");
        savead.loadAd(new AdRequest.Builder().build());

        sharead = new InterstitialAd(MainActivity.this);
        sharead.setAdUnitId("ca-app-pub-2369748974411795/2271698132");
        sharead.loadAd(new AdRequest.Builder().build());

        nextadd = new InterstitialAd(MainActivity.this);
        nextadd.setAdUnitId("ca-app-pub-2369748974411795/4697031989");
        nextadd.loadAd(new AdRequest.Builder().build());

        //ca-app-pub-3940256099942544/5224354917
        //ca-app-pub-2369748974411795/5606284392
        //nextad.setRewardedVideoAdListener(this);

        /*Constraints constraints = new Constraints.Builder()
                .build();

        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(MainActivity.this)
                .enqueue(saveRequest);*/

        if (rate.equals("yes"))
        {
            TimerTask timerTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("rate", "no").apply();
                            showRateDialog();
                        }
                    });
                }
            };
            new Timer().schedule(timerTask, 30000);
        }

        progressDialog = new ProgressDialog(MainActivity.this);
        if (lan.equals("arabic"))
        {
            progressDialog.setMessage("برجاء الانتظار");
        } else if (lan.equals("english"))
        {
            progressDialog.setMessage("Wait ...");
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        init();

        MobileAds.initialize(MainActivity.this, new OnInitializationCompleteListener()
        {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus)
            {

            }
        });
    }

    private void init()
    {
        v = findViewById(R.id.main);
        next = findViewById(R.id.next_btn);
        back = findViewById(R.id.back_btn);
        save = findViewById(R.id.save_btn);
        msg_body = findViewById(R.id.msg_body);

        more = findViewById(R.id.more_fab);

        imageView = findViewById(R.id.main_img);
        linearLayout = findViewById(R.id.main_lin);
        face_lin = findViewById(R.id.face_lin);

        c1 = findViewById(R.id.card1);
        c2 = findViewById(R.id.card2);
        cback = findViewById(R.id.cardback);

        cback.setVisibility(View.GONE);

        if (lan.equals("arabic"))
        {
            Typeface typeface = ResourcesCompat.getFont(MainActivity.this, R.font.cas);
            next.setTypeface(typeface);
            back.setTypeface(typeface);
            save.setTypeface(typeface);

            next.setText("الرسالة التاليه");
            back.setText("الرسالة السابقة");
            save.setText("حفظ");
        } else if (lan.equals("english"))
        {
            Typeface typeface = ResourcesCompat.getFont(MainActivity.this, R.font.bs);
            next.setTypeface(typeface);
            back.setTypeface(typeface);
            save.setTypeface(typeface);

            next.setText("next message");
            back.setText("previous message");
            save.setText("save");
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        imgs = new ArrayList<>();
        messageModels = new ArrayList<>();

        imgs.add(R.drawable.a);
        imgs.add(R.drawable.b);
        imgs.add(R.drawable.c);
        imgs.add(R.drawable.d);
        imgs.add(R.drawable.e);
        imgs.add(R.drawable.f);
        imgs.add(R.drawable.g);
        imgs.add(R.drawable.h);
        imgs.add(R.drawable.i);
        imgs.add(R.drawable.j);
        imgs.add(R.drawable.k);
        imgs.add(R.drawable.l);
        imgs.add(R.drawable.m);
        imgs.add(R.drawable.n);
        imgs.add(R.drawable.back);

        Random rand = new Random();
        int i = imgs.get(rand.nextInt(imgs.size()));
        imageView.setImageResource(i);

        save.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(final View v)
            {
                if (savead.isLoaded())
                {
                    savead.show();
                } else
                    {
                        savead.loadAd(new AdRequest.Builder().build());
                        savead.show();
                    }

                if (lan.equals("arabic"))
                {
                    msg = "هل انت واثق من حفظ الصورة ؟";
                    yes = "نعم";
                    no = "لا";
                } else if (lan.equals("english"))
                {
                    msg = "are you sure to save this picture ?";
                    yes = "yes";
                    no = "no";
                }

                alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setMessage(msg)
                        .setPositiveButton(yes, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                linearLayout.setVisibility(View.GONE);
                                savetointenral(v);
                            }
                        })

                        .setNegativeButton(no, null)
                        .show();
            }
        });

        more.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v)
            {
                if (sharead.isLoaded())
                {
                    sharead.show();
                } else
                {
                    sharead.loadAd(new AdRequest.Builder().build());
                    sharead.show();
                }

                linearLayout.setVisibility(View.GONE);
                screenshoot(v);
            }
        });

        next.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (nextadd.isLoaded())
                {
                    nextadd.show();
                } else
                {
                    nextadd.loadAd(new AdRequest.Builder().build());
                    nextadd.show();
                }

                Random rand = new Random();
                int iii = imgs.get(rand.nextInt(imgs.size()));
                imageView.setImageResource(iii);

                if (counter == 0)
                {
                    MessageModel i = messageModels.get(1);
                    msg_body.setText(i.getBody());
                    counter = 1;
                    cback.setVisibility(View.VISIBLE);
                } else if (counter == 1)
                {
                    MessageModel i = messageModels.get(2);
                    msg_body.setText(i.getBody());
                    counter = 2;
                    c1.setVisibility(View.GONE);

                    TimerTask timerTask = new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            runOnUiThread(new Runnable()
                            {
                                public void run()
                                {
                                    showRateDialog3();
                                }
                            });
                        }
                    };
                    new Timer().schedule(timerTask, 3000);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Random rand = new Random();
                int iii = imgs.get(rand.nextInt(imgs.size()));
                imageView.setImageResource(iii);

                if (counter == 1)
                {
                    MessageModel i = messageModels.get(0);
                    msg_body.setText(i.getBody());
                    counter = 0;
                    cback.setVisibility(View.GONE);
                } else if (counter == 2)
                {
                    MessageModel i = messageModels.get(1);
                    msg_body.setText(i.getBody());
                    counter = 1;
                    c1.setVisibility(View.VISIBLE);
                }
            }
        });

        progressDialog.show();
        //progressDialog.setCancelable(false);

        getmessages();
    }

    private void getmessages()
    {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        String currentDateandTime = sdf.format(new Date());

        databaseReference.child("messages").child(currentDateandTime).child(gender).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                progressDialog.dismiss();
                messageModels.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    MessageModel messageModel = dataSnapshot1.getValue(MessageModel.class);
                    messageModels.add(messageModel);
                }

                if (messageModels != null && messageModels.size() != 0)
                {
                    MessageModel i = messageModels.get(0);
                    msg_body.setText(i.getBody());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                progressDialog.dismiss();
            }
        });
    }

    public Bitmap viewToBitmap(View view)
    {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public void savetoo(Bitmap bitmap)
    {
        try {
            FileOutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/path/to/file.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed()
    {
        //rateDialog2();
        showRateDialog1();
    }

    @SuppressLint("RestrictedApi")
    void shareimg()
    {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        //Target whatsapp:
        //Add text and then Image URI
        shareIntent.putExtra(Intent.EXTRA_TEXT, "قرارك اليوم ..");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/jpeg");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try
        {
            startActivity(Intent.createChooser(shareIntent,"Share via"));

            linearLayout.setVisibility(View.VISIBLE);
            face_lin.setVisibility(View.VISIBLE);
        } catch (android.content.ActivityNotFoundException ex)
        {
            //ToastHelper.MakeShortText("Whatsapp have not been installed.");
        }
    }

    public void screenshoot(View view)
    {


        Bitmap bitmap = ScreenShoot.takescreenshootofrootview(v);
        uri = getImageUri(MainActivity.this, bitmap);

        shareimg();
    }

    @SuppressLint("RestrictedApi")
    public void savetointenral(View v)
    {
        Bitmap bitmap = ScreenShoot.takescreenshootofrootview(v);
        uri = getImageUri(MainActivity.this, bitmap);

        saveToInternalStorage(bitmap,uri);
    }

    @SuppressLint("RestrictedApi")
    private void saveToInternalStorage(Bitmap bitmapImage, Uri uri)
    {
        File filepath = Environment.getExternalStorageDirectory();

        // Create a new folder in SD Card
        File dir = new File(filepath.getAbsolutePath()
                + "/Ur Decision/");
        dir.mkdirs();

        // Create a name for the saved image
        File file = new File(dir, uri.getLastPathSegment() + ".png");

        try {
            FileOutputStream fos = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //save.setClickable(true);
            if (lan.equals("arabic"))
            {
                Toast.makeText(getApplicationContext(), "تم الحفظ ..", Toast.LENGTH_SHORT).show();
            } else if (lan.equals("english"))
            {
                Toast.makeText(getApplicationContext(), "saved ..", Toast.LENGTH_SHORT).show();
            }
            linearLayout.setVisibility(View.VISIBLE);
            face_lin.setVisibility(View.VISIBLE);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /*@Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted()
    {
        next.setEnabled(false);
        next.setClickable(false);

        back.setEnabled(false);
        back.setClickable(false);
    }

    @Override
    public void onRewardedVideoAdClosed()
    {
        next.setEnabled(false);
        next.setClickable(false);

        back.setEnabled(false);
        back.setClickable(false);
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted()
    {
        Random rand = new Random();
        int iii = imgs.get(rand.nextInt(imgs.size()));
        imageView.setImageResource(iii);

        if (counter == 0)
        {
            MessageModel i = messageModels.get(1);
            msg_body.setText(i.getBody());
            counter = 1;
            cback.setVisibility(View.VISIBLE);
        } else if (counter == 1)
        {
            MessageModel i = messageModels.get(2);
            msg_body.setText(i.getBody());
            counter = 2;
            c1.setVisibility(View.GONE);
        }
    }*/

    public void rateDialog ()
    {
        if (lan.equals("arabic"))
        {
            msg = "تقييم التطبيق.";
            yes = "تقييم";
            no = "لا";
        } else if (lan.equals("english"))
        {
            msg = "Rate Application.";
            yes = "yes";
            no = "no";
        }

        alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setMessage(msg)
                .setPositiveButton(yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=softagi.urdecision"); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                })

                .setNegativeButton(no, null)
                .show();
    }

    public void rateDialog2 ()
    {
        if (lan.equals("arabic"))
        {
            msg = "تقييم التطبيق.";
            yes = "تقييم";
            no = "خروج";
        } else if (lan.equals("english"))
        {
            msg = "Rate Application.";
            yes = "yes";
            no = "exit";
        }

        alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setMessage(msg)
                .setPositiveButton(yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=softagi.urdecision"); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                })

                .setNegativeButton(no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        finishAffinity();
                    }
                })
                .show();
    }

    private void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = "channel";
            String description = "desc";
            int importance = NotificationManager.IMPORTANCE_MAX;
            @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null)
            {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showRateDialog1()
    {
        final Dialog dialog = new Dialog(MainActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.rate_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView title = dialog.findViewById(R.id.rate_title);
        TextView body = dialog.findViewById(R.id.rate_body);
        Button yes_btn = dialog.findViewById(R.id.yes_btn);
        Button cancel_btn = dialog.findViewById(R.id.cancel_btn);

        if (lan.equals("arabic"))
        {
            title.setText("تقييم التطبيق");
            body.setText("اذا اعجبتك رسائلنا لا تتردد في ان تدعمنا بتقييمك لنستمر دائما في اسعادك \uD83C\uDF38");
            yes_btn.setText("تقييم");
            cancel_btn.setText("خروج");
        } else if (lan.equals("english"))
        {
            title.setText("Rate App");
            body.setText("اذا اعجبتك رسائلنا لا تتردد في ان تدعمنا بتقييمك لنستمر دائما في اسعادك \uD83C\uDF38");
            yes_btn.setText("rate");
            cancel_btn.setText("exit");
        }

        yes_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=softagi.urdecision"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finishAffinity();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void showRateDialog()
    {
        final Dialog dialog = new Dialog(MainActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.rate_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView title = dialog.findViewById(R.id.rate_title);
        TextView body = dialog.findViewById(R.id.rate_body);
        Button yes_btn = dialog.findViewById(R.id.yes_btn);
        Button cancel_btn = dialog.findViewById(R.id.cancel_btn);

        if (lan.equals("arabic"))
        {
            title.setText("تقييم التطبيق");
            body.setText("اذا اعجبتك رسائلنا لا تتردد في ان تدعمنا بتقييمك لنستمر دائما في اسعادك \uD83C\uDF38");
            yes_btn.setText("تقييم");
            cancel_btn.setText("لا");
        } else if (lan.equals("english"))
        {
            title.setText("Rate App");
            body.setText("اذا اعجبتك رسائلنا لا تتردد في ان تدعمنا بتقييمك لنستمر دائما في اسعادك \uD83C\uDF38");
            yes_btn.setText("rate");
            cancel_btn.setText("no");
        }

        yes_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=softagi.urdecision"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void showRateDialog3()
    {
        final Dialog dialog = new Dialog(MainActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.msg_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView title = dialog.findViewById(R.id.rate_title);
        TextView body = dialog.findViewById(R.id.rate_body);
        Button cancel_btn = dialog.findViewById(R.id.cancel_btn);

        if (lan.equals("arabic"))
        {
            title.setText("قرار اليوم");
            body.setText("لقد انتهت رسائلك لهذا اليوم، عد غداً لترى رسائلك الجديدة \uD83C\uDF38");
            cancel_btn.setText("حسنا");
        } else if (lan.equals("english"))
        {
            title.setText("Ur Decision");
            body.setText("لقد انتهت رسائلك لهذا اليوم، عد غداً لترى رسائلك الجديدة \uD83C\uDF38");
            cancel_btn.setText("okay");
        }

        cancel_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
}