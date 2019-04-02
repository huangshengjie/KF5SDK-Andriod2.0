package com.kf5sdk.exam;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kf5.sdk.helpcenter.ui.HelpCenterActivity;
import com.kf5.sdk.im.entity.CardConstant;
import com.kf5.sdk.im.ui.KF5ChatActivity;
import com.kf5.sdk.system.entity.Field;
import com.kf5.sdk.system.entity.ParamsKey;
import com.kf5.sdk.system.init.UserInfoAPI;
import com.kf5.sdk.system.internet.HttpRequestCallBack;
import com.kf5.sdk.system.utils.LogUtil;
import com.kf5.sdk.system.utils.SPUtils;
import com.kf5.sdk.system.utils.SafeJson;
import com.kf5.sdk.ticket.ui.FeedBackActivity;
import com.kf5.sdk.ticket.ui.LookFeedBackActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.SoftReference;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        kf5Login();
    }

    private void initWidgets() {
        findViewById(R.id.tvHelpCenter).setOnClickListener(this);
        findViewById(R.id.tvFeedBack).setOnClickListener(this);
        findViewById(R.id.tvLookFeedBack).setOnClickListener(this);
        findViewById(R.id.tvIM).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvHelpCenter:
                startActivity(new Intent(this, HelpCenterActivity.class));
                break;
            case R.id.tvFeedBack:
                startActivity(new Intent(this, FeedBackActivity.class));
                break;
            case R.id.tvLookFeedBack:
                startActivity(new Intent(this, LookFeedBackActivity.class));
                break;
            case R.id.tvIM:
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(CardConstant.LINK_URL, "www.baidu.com");
                    jsonObject.put(CardConstant.TITLE, "京东平台卖家销售并发货的商品，由平台卖家提供发票和相应的售后服务。请您放心购买！注：因厂家会在没有任何提前通知的情况下更改产品包装、产地或者一些附件，本司不能确保客户收到的货物与商城图片、产地、附件说明完全一致。只能确保为原厂正货！并且保证与当时市场上同样主流新品一致。若本商城没有及时更新，请大家谅解！");
                    jsonObject.put(CardConstant.IMG_URL, "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1504695965182&di=5b4e8b755cebd1abf17341215f89b5c6&imgtype=0&src=http%3A%2F%2Fimghr.heiguang.net%2F3%2F2014%2F0707%2F2014070753ba66264d67a2.jpg");
                    jsonObject.put(CardConstant.PRICE, "123456");
                    jsonObject.put(CardConstant.LINK_TITLE, "发送东西");
                    Intent intent;
                    intent = new Intent(this, KF5ChatActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void kf5Login() {
        SPUtils.clearSP();

        final Map<String, String> map = new ArrayMap<>();
        map.put("name", "采集员小黄");// 用户昵称
        map.put("remark_name", "采集员小黄");// 用户备注姓名
        map.put("email", "daniel.huang.prc@gmail.com");// 邮箱
        map.put("phone", "18888888888");// 用户手机
        map.put("agent_display_name", "黑人客服部");// 客服处理工单时显示名称

        SPUtils.saveAppID("0015c9b6cf93b698ea584cf20f93e9537ac3ec1439b0530b");
        SPUtils.saveHelpAddress("shelf-kefu.kf5.com");

        // 这里是传入用户自定义字段信息，同时更新用户信息接口也可以设置用户自定义字段。
//        JSONArray jsonArray = new JSONArray();
//
//        try {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put(ParamsKey.NAME, "field_1000572");
//            jsonObject.put(ParamsKey.VALUE, "这里是测试字段");
//            jsonArray.put(jsonObject);
//
//            JSONObject jsonObject1 = new JSONObject();
//            jsonObject1.put(ParamsKey.NAME, "field_1000573");
//            jsonObject1.put(ParamsKey.VALUE, "这里是文本区域");
//            jsonArray.put(jsonObject1);
//
//            JSONObject jsonObject2 = new JSONObject();
//            jsonObject2.put(ParamsKey.NAME, "field_1000574");
//            jsonObject2.put(ParamsKey.VALUE, "1");
//            jsonArray.put(jsonObject2);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        map.put(ParamsKey.USER_FIELDS, jsonArray.toString());

        LogUtil.printf(map.toString());
        SPUtils.saveUserAgent(getAgent(new SoftReference<>(this)));
        UserInfoAPI.getInstance().createUser(map
                , new HttpRequestCallBack() {
                    @Override
                    public void onSuccess(final String result) {
                        runOnUiThread(() -> {
                            try {
                                final JSONObject jsonObject = SafeJson.parseObj(result);
                                int resultCode = SafeJson.safeInt(jsonObject, "error");
                                if (resultCode == 0) {
                                    final JSONObject dataObj = SafeJson.safeObject(jsonObject, Field.DATA);
                                    JSONObject userObj = SafeJson.safeObject(dataObj, Field.USER);
                                    if (userObj != null) {
                                        String userToken = userObj.getString(Field.USERTOKEN);
                                        int id = userObj.getInt(Field.ID);
                                        SPUtils.saveUserToken(userToken);
                                        SPUtils.saveUserId(id);
                                        saveToken(map);
                                    }
                                } else {
                                    runOnUiThread(() -> {
                                        loginUser(map);
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String result) {
                        Toast.makeText(MainActivity.this, "连接客服系统失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveToken(Map<String, String> map) {
        map.put(ParamsKey.DEVICE_TOKEN, "123456");//设备号
        UserInfoAPI.getInstance().saveDeviceToken(map, new HttpRequestCallBack() {
            @Override
            public void onSuccess(String result) {
                Log.i("kf5测试", "保存设备Token成功" + result);
            }

            @Override
            public void onFailure(String result) {
                Log.i("kf5测试", "保存设备Token失败" + result);
            }
        });

        UserInfoAPI.getInstance().getUserInfo(map, new HttpRequestCallBack() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onFailure(String result) {

            }
        });
    }

    private void loginUser(final Map<String, String> map) {
        UserInfoAPI.getInstance().loginUser(map, new HttpRequestCallBack() {
            @Override
            public void onSuccess(final String result) {
                runOnUiThread(() -> {
                    try {
                        final JSONObject jsonObject = SafeJson.parseObj(result);
                        int resultCode = SafeJson.safeInt(jsonObject, "error");
                        if (resultCode == 0) {
                            final JSONObject dataObj = SafeJson.safeObject(jsonObject, Field.DATA);
                            JSONObject userObj = SafeJson.safeObject(dataObj, Field.USER);
                            if (userObj != null) {
                                String userToken = userObj.getString(Field.USERTOKEN);
                                int id = userObj.getInt(Field.ID);
                                SPUtils.saveUserToken(userToken);
                                SPUtils.saveUserId(id);
                                saveToken(map);
                            }
                        } else {
                            runOnUiThread(() -> {
                                //String message = jsonObject.getString("message");
                                Toast.makeText(MainActivity.this, "连接客服系统失败", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(String result) {
                Toast.makeText(MainActivity.this, "连接客服系统失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String getAgent(SoftReference<Context> softReference) {

        String agent = "";
        try {
            String ua = System.getProperty("http.agent");
            String packageName = softReference.get().getPackageName();
            PackageInfo info = softReference.get().getPackageManager().getPackageInfo(packageName, 0);
            agent = ua + ", " + packageName + "/" + info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agent;
    }
}
