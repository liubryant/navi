package cn.navibeidou.beidou.okhttp.builder;


import org.json.JSONObject;

import cn.navibeidou.beidou.okhttp.request.PostStringRequest;
import cn.navibeidou.beidou.okhttp.request.RequestCall;
import okhttp3.MediaType;

/**
 * Created by zhy on 15/12/14.
 */
public class PostStringBuilder extends OkHttpRequestBuilder<PostStringBuilder> {
    private String content;
    private JSONObject jsonObject;
    private MediaType mediaType;


    public PostStringBuilder content(String content) {
        this.content = content;
        return this;
    }

    public PostStringBuilder contentJson(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        return this;
    }

    public PostStringBuilder mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    @Override
    public RequestCall build() {
        return new PostStringRequest(url, tag, params, headers, content, mediaType, id).build();
    }


}
