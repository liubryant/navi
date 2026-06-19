package cn.navibeidou.beidou.world;

import cn.navibeidou.beidou.R;

public class CloudPanoramaItem {
    public final String title;
    public final String url;
    public final int coverResId;

    public CloudPanoramaItem(String title, String url, int coverResId) {
        this.title = title;
        this.url = url;
        this.coverResId = coverResId;
    }

    public static CloudPanoramaItem[] all() {
        return new CloudPanoramaItem[]{
                new CloudPanoramaItem("北京天坛", "https://www.720yun.com/t/83vkcli708q?scene_id=59434172", R.drawable.cloud_panorama_01),
                new CloudPanoramaItem("清华大学", "https://www.720yun.com/vr/85c24wagung", R.drawable.cloud_panorama_02),
                new CloudPanoramaItem("贡嘎攀登", "https://www.720yun.com/vr/792jkrtnev5", R.drawable.cloud_panorama_08),
                new CloudPanoramaItem("稻城亚丁", "https://www.720yun.com/t/9a4j5gtkuy0?scene_id=11843262", R.drawable.cloud_panorama_11),
                new CloudPanoramaItem("上海陆家嘴", "https://www.720yun.com/t/59vkbyplr7q?scene_id=90167954", R.drawable.cloud_panorama_16),
                new CloudPanoramaItem("黄山风景区", "https://www.720yun.com/t/favkte8w0fb?scene_id=69676267", R.drawable.cloud_panorama_14),
                new CloudPanoramaItem("橘子洲头", "https://www.720yun.com/t/f562c9zfuci?scene_id=1589907", R.drawable.cloud_panorama_13),
                new CloudPanoramaItem("全景看北京", "https://www.720yun.com/t/942jOryutu8?scene_id=2095322", R.drawable.cloud_panorama_18),
                new CloudPanoramaItem("冰雪世界", "https://www.720yun.com/vr/cccj5syntn3", R.drawable.cloud_panorama_03),
                new CloudPanoramaItem("元宇宙艺术展", "https://www.720yun.com/vr/28a2eqiuwcr", R.drawable.cloud_panorama_04),
                new CloudPanoramaItem("秦始皇兵马俑", "https://www.720yun.com/t/07cjrOhfzk4?scene_id=28286004", R.drawable.cloud_panorama_05),
                new CloudPanoramaItem("深圳像素摄影", "https://www.720yun.com/t/35vkcmdlpqb?scene_id=67060540", R.drawable.cloud_panorama_06),
                new CloudPanoramaItem("拉萨布达拉宫", "https://www.720yun.com/vr/c8027wsg9br", R.drawable.cloud_panorama_07),
                new CloudPanoramaItem("北京故宫", "https://www.720yun.com/t/942jOryutu8?scene_id=2095322", R.drawable.cloud_panorama_09),
                new CloudPanoramaItem("泰国曼谷", "https://www.720yun.com/t/74b22jidaen?scene_id=343404", R.drawable.cloud_panorama_10),
                new CloudPanoramaItem("故宫雪景", "https://www.720yun.com/t/df4jussOrw1?scene_id=60406967", R.drawable.cloud_panorama_12),
                new CloudPanoramaItem("广州塔", "https://www.720yun.com/t/35vkOm7lgqe?scene_id=56460523", R.drawable.cloud_panorama_15),
                new CloudPanoramaItem("邓紫棋演唱会场馆", "https://www.720yun.com/vr/d12j57ekuv9", R.drawable.cloud_panorama_17)
        };
    }
}
