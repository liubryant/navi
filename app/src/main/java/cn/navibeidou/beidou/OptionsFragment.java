package cn.navibeidou.beidou;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.LinkedList;
import java.util.List;

import cn.navibeidou.beidou.Util.CommonUtil;
import cn.navibeidou.beidou.Util.ListAdapter;
import cn.navibeidou.beidou.Util.ListItem;

public class OptionsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private SimpleToolbar mSimpleToolbar;
    private TextView mVer;
    private List<ListItem> mData = null;
    private Context mContext;
    private ListAdapter mAdapter = null;
    private ListView list_option;
    //    private TTAdNative mTTAdNative;
    private boolean mIsLoading = false;
    private Dialog mAdDialog;
    private ImageView mAdImageView;
    private ImageView mCloseImageView;
    private ViewGroup mRootView;
    private TextView mDislikeView;
    private FrameLayout mBannerContainer;
    private Button mCreativeButton;
    //    private TTAdDislike mTTAdDislike;
//    private TTNativeExpressAd mTTAd;
    private long startTime = 0;
    private boolean mHasShowDownloadActive = false;

    public OptionsFragment() {
    }

    public static OptionsFragment newInstance(String param1, String param2) {
        OptionsFragment fragment = new OptionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //step2:创建TTAdNative对象,用于调用Ad请求接口
//        mTTAdNative = TTAdManagerHolder.get().createAdNative(getActivity());
//        //step3:可选，申请部分权限，如read_phone_state,防止获取不了imei时候，下载类Ad没有填充的问题。
//        TTAdManagerHolder.get().requestPermissionIfNecessary(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_options, container, false);//inflater.inflate(R.layout.fragment_options, null);
        mSimpleToolbar = view.findViewById(R.id.simple_toolbar);
        mSimpleToolbar.setMainTitle(getArguments().getString(ARG_PARAM1));

        list_option = (ListView) view.findViewById(R.id.list_option);
//        mBannerContainer = (FrameLayout) getActivity().findViewById(R.id.banner_container);
        //动态加载顶部View和底部View
        //final LayoutInflater inflater = LayoutInflater.from(this);
        View headView = inflater.inflate(R.layout.view_header, null, false);
        //View footView = inflater.inflate(R.layout.view_footer, null, false);
        mVer = headView.findViewById(R.id.textViewVer);
        String ver = "Ver" + ":" + CommonUtil.getAppVersionName(mContext) + "(" + CommonUtil.getAppVersionCode(mContext) + ")";
        mVer.setText(ver);

        mData = new LinkedList<ListItem>();
//        mData.add(new ListItem(getResources().getString(R.string.options_tag1), null, 0));
//        mData.add(new ListItem(getResources().getString(R.string.options_1), getResources().getString(R.string.options_sub1), 1));
        mData.add(new ListItem(getResources().getString(R.string.promot), null, 0));
        mData.add(new ListItem(getResources().getString(R.string.promotegift), getResources().getString(R.string.mypromote), 1));

//        mData.add(new ListItem(getResources().getString(R.string.options_tag3), null, 0));
//        mData.add(new ListItem(getResources().getString(R.string.options_3), getResources().getString(R.string.options_sub2), 1));
        mAdapter = new ListAdapter((LinkedList<ListItem>) mData, mContext);
        //添加表头和表尾需要写在setAdapter方法调用之前！！！
        list_option.addHeaderView(headView);
        //list_option.addFooterView(footView);

        list_option.setAdapter(mAdapter);
        list_option.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {//arg0，即parent：// arg1,即view： // arg2,即position： // arg3，即id：
                //Log.i("ListView: ", "onItemClick: "+"arg0:"+arg0+"arg1:"+arg1+"arg2:"+arg2+"arg3:"+arg3);
                //Log.i("ListView: ", "onItemClick: "+"arg2:"+arg2+" arg3:"+arg3);

                if (arg3 >= 0) {
                    ListItem item = mData.get((int) arg3);
                    Log.i("ListView: ", "onItemClick: " + item.getTitle());
                    if (item.getTitle().equals(getResources().getString(R.string.promotegift))) {
//                        startActivity(new Intent(mContext, DeviceInfoActivity.class));
//                        loadExpressAd("945191419", 450, 300);
//                        loadInteractionAd("901121435");
                    }
                }

            }
        });
        //加载bannerAd
//        loadBannerAd("945365975");
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCreativeButton != null) {
            mCreativeButton = null;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
