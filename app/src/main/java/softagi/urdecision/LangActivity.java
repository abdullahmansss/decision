package softagi.urdecision;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class LangActivity extends AppCompatActivity
{
    ImageView imageView;
    Button ar,en,next;
    int language = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lang);

        imageView = findViewById(R.id.lang_img);
        ar = findViewById(R.id.ar_btn);
        en = findViewById(R.id.eng_btn);
        next = findViewById(R.id.next_btn);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void next(View view)
    {
        if (language == 1)
        {
            PreferenceManager.getDefaultSharedPreferences(LangActivity.this).edit().putString("lan", "arabic").apply();
        } else if (language == 0)
        {
            PreferenceManager.getDefaultSharedPreferences(LangActivity.this).edit().putString("lan", "english").apply();
        }

        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        intent.putExtra("lang", language);
        startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    public void english(View view)
    {
        imageView.setImageResource(R.drawable.eng);
        ar.setAlpha(.5f);
        en.setAlpha(1);
        language = 0;

        Typeface typeface = ResourcesCompat.getFont(LangActivity.this, R.font.bs);
        next.setText("next");
        next.setTypeface(typeface);
    }

    public void arabic(View view)
    {
        imageView.setImageResource(R.drawable.ar);
        en.setAlpha(.5f);
        ar.setAlpha(1);
        language = 1;

        Typeface typeface = ResourcesCompat.getFont(LangActivity.this, R.font.cas);
        next.setText("التالي");
        next.setTypeface(typeface);
    }
}
