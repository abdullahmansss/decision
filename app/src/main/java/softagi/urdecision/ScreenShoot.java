package softagi.urdecision;

import android.graphics.Bitmap;
import android.view.View;

class ScreenShoot
{
    private static Bitmap takescreenshoot(View view)
    {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    static Bitmap takescreenshootofrootview(View view)
    {
        return takescreenshoot(view.getRootView());
    }
}
