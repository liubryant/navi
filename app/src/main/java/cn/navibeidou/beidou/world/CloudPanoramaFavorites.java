package cn.navibeidou.beidou.world;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/** 720云景区收藏状态的本地存储，按 {@link CloudPanoramaItem#id}（封面文件名）去重。 */
public class CloudPanoramaFavorites {
    private static final String PREFS_NAME = "cloud_panorama_favorites";
    private static final String KEY_IDS = "favorite_ids";

    public static boolean isFavorite(Context context, String id) {
        return prefs(context).getStringSet(KEY_IDS, new HashSet<String>()).contains(id);
    }

    public static void setFavorite(Context context, String id, boolean favorite) {
        Set<String> current = new HashSet<String>(prefs(context).getStringSet(KEY_IDS, new HashSet<String>()));
        if (favorite) {
            current.add(id);
        } else {
            current.remove(id);
        }
        prefs(context).edit().putStringSet(KEY_IDS, current).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
