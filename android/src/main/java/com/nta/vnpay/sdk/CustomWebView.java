package com.nta.vnpay.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
//import android.webkit.SslErrorHandler;
//import android.webkit.WebResourceError;
//import android.webkit.WebResourceRequest;
//import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ZoomButtonsController;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.vnpay.authentication.VNP_AuthenticationActivity;
import com.vnpay.authentication.VNP_SdkCompletedCallback;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CustomWebView extends Activity {

    private WebView wvContent;
    private String url = "";
    private String scheme = "";
    private String tmn_code = "";
    private boolean is_sandbox = false;
    private VNP_BankEntity[] entity_Response;
    private ProgressDialog dialog;
    private LinearLayout llLoading;
    private static VNP_SdkCompletedCallback sdkCompletedCallback;
    String Url = "https://pay.vnpay.vn/qrpayauth/api/sdk/get_qrpay_support/";
    String Url_sandbox = "https://sandbox.vnpayment.vn/qrpayauth/api/sdk/get_qrpay_support";
    String Host = "pay.vnpay.vn";
    String Host_sandbox = "sandbox.vnpayment.vn";

    public CustomWebView() {
    }

    public static void setSdkCompletedCallback(VNP_SdkCompletedCallback sdkCompletedCallback) {
        CustomWebView.sdkCompletedCallback = sdkCompletedCallback;
    }

    public void onBackPressed() {
        if (sdkCompletedCallback != null) {
            sdkCompletedCallback.sdkAction("AppBackAction");
        }

        this.finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wvContent = new WebView(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        wvContent.setLayoutParams(layoutParams);
        this.setContentView(wvContent);

        wvContent.loadUrl("https://abhiandroid.com/ui/webview");

        try {
            Bundle bundle = this.getIntent().getExtras();
            if (bundle != null) {
                if (bundle.containsKey("url")) {
                    this.url = bundle.getString("url");
                    if (TextUtils.isEmpty(this.url)) {
                        Toast.makeText(this, "Thiếu tham số", Toast.LENGTH_LONG).show();
                        this.setResult(-1);
                        this.finish();
                    }

                    Log.wtf("SDK", this.url);
                } else {
                    Toast.makeText(this, "Thiếu tham số", Toast.LENGTH_LONG).show();
                    this.setResult(-1);
                    this.finish();
                }

                if (bundle.containsKey("scheme")) {
                    this.scheme = bundle.getString("scheme");
                    if (TextUtils.isEmpty(this.scheme)) {
                        Toast.makeText(this, "Thiếu tham số", Toast.LENGTH_LONG).show();
                        this.setResult(-1);
                        this.finish();
                    } else {
                        if (!this.scheme.endsWith("://")) {
                            this.scheme = this.scheme + "://sdk";
                        }

                        Log.wtf("SDK", this.scheme);
                    }
                }

                if (bundle.containsKey("tmn_code")) {
                    this.tmn_code = bundle.getString("tmn_code");
                    if (TextUtils.isEmpty(this.tmn_code)) {
                        Toast.makeText(this, "Thiếu tham số", Toast.LENGTH_LONG).show();
                        this.setResult(-1);
                        this.finish();
                    }

                    Log.wtf("SDK", this.tmn_code);
                } else {
                    Toast.makeText(this, "Thiếu tham số", Toast.LENGTH_LONG).show();
                    this.setResult(-1);
                    this.finish();
                }

                if (bundle.containsKey("is_sandbox")) {
                    this.is_sandbox = bundle.getBoolean("is_sandbox");
                    Log.wtf("SDK is_sandbox", this.is_sandbox ? "true" : "false");
                }

                this.wvContent.getSettings().setJavaScriptEnabled(true);
                this.wvContent.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                this.wvContent.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
                this.wvContent.setInitialScale(1);
                this.wvContent.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
                this.wvContent.getSettings().setLoadWithOverviewMode(true);
                this.wvContent.getSettings().setUseWideViewPort(true);
                this.wvContent.getSettings().setSupportZoom(true);
                this.wvContent.getSettings().setDomStorageEnabled(true);
                this.wvContent.getSettings().setBuiltInZoomControls(true);
                if (Build.VERSION.SDK_INT >= 11) {
                    (new Runnable() {
                        @TargetApi(11)
                        public void run() {
                            CustomWebView.this.wvContent.getSettings().setDisplayZoomControls(false);
                        }
                    }).run();
                } else {
                    try {
                        ZoomButtonsController zoom_controll = (ZoomButtonsController)this.wvContent.getClass().getMethod("getZoomButtonsController").invoke(this.wvContent);
                        zoom_controll.getContainer().setVisibility(View.GONE);
                    } catch (IllegalAccessException var5) {
                        var5.printStackTrace();
                    } catch (InvocationTargetException var6) {
                        var6.printStackTrace();
                    } catch (NoSuchMethodException var7) {
                        var7.printStackTrace();
                    }
                }

                this.wvContent.setWebViewClient(new CustomWebView.myWebClient());
                this.wvContent.loadUrl(this.url);
            }

            try {
                if (this.dialog == null || !this.dialog.isShowing()) {
                    this.dialog = new ProgressDialog(this);
                }

                this.dialog.setMessage("Xin doi trong giay lat");
                this.dialog.show();
            } catch (Exception var8) {
                Log.wtf("SDK", var8.getMessage());
            }

            (new Thread(new Runnable() {
                public void run() {
                    try {
                        OkHttpClient client = (new OkHttpClient()).newBuilder().connectTimeout(30L, TimeUnit.SECONDS).writeTimeout(80L, TimeUnit.SECONDS).readTimeout(80L, TimeUnit.SECONDS).build();
                        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                        RequestBody body = RequestBody.create(mediaType, "tmn_code=" + CustomWebView.this.tmn_code + "&os_type=ANDROID");
                        Request request = (new Request.Builder()).url(CustomWebView.this.is_sandbox ? CustomWebView.this.Url_sandbox : CustomWebView.this.Url).post(body).addHeader("Content-Type", "application/x-www-form-urlencoded").addHeader("cache-control", "no-cache,no-cache").addHeader("Accept", "*/*").addHeader("Host", CustomWebView.this.is_sandbox ? CustomWebView.this.Host_sandbox : CustomWebView.this.Host).addHeader("accept-encoding", "gzip, deflate").addHeader("content-length", "33").addHeader("Connection", "keep-alive").build();
                        Response response = client.newCall(request).execute();
                        Gson gson = (new GsonBuilder()).disableHtmlEscaping().create();
                        StringReader stringReader = new StringReader(response.body().string());
                        JsonReader reader = new JsonReader(stringReader);
                        CustomWebView.this.entity_Response = (VNP_BankEntity[])gson.fromJson(reader, VNP_BankEntity[].class);
                        Log.wtf("Tag", "Success");
                        if (CustomWebView.this.dialog != null) {
                            if (CustomWebView.this.dialog.isShowing()) {
                                CustomWebView.this.dialog.dismiss();
                            }

                            CustomWebView.this.dialog = null;
                        }
                    } catch (Exception var9) {
                        var9.printStackTrace();
                        Log.wtf("SDK", var9.getMessage());
                        if (CustomWebView.this.dialog != null) {
                            if (CustomWebView.this.dialog.isShowing()) {
                                CustomWebView.this.dialog.dismiss();
                            }

                            CustomWebView.this.dialog = null;
                        }
                    }

                }
            })).start();
        } catch (Exception var9) {
            var9.printStackTrace();
            Log.wtf("SDK", var9.getMessage());
            if (this.dialog != null) {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }

                this.dialog = null;
            }
        }
    }

    private static Uri replaceUriParameter(Uri uri, String key, String newValue) {
        Set<String> params = uri.getQueryParameterNames();
        android.net.Uri.Builder newUri = uri.buildUpon().clearQuery();
        Iterator var5 = params.iterator();

        while(var5.hasNext()) {
            String param = (String)var5.next();
            newUri.appendQueryParameter(param, param.equals(key) ? newValue : uri.getQueryParameter(param));
        }

        return newUri.build();
    }

    private static Uri addUriParameter(Uri uri, String key, String newValue) {
        Set<String> params = uri.getQueryParameterNames();
        android.net.Uri.Builder newUri = uri.buildUpon().clearQuery();
        Iterator var5 = params.iterator();

        while(var5.hasNext()) {
            String param = (String)var5.next();
            newUri.appendQueryParameter(param, uri.getQueryParameter(param));
        }

        newUri.appendQueryParameter(key, newValue);
        return newUri.build();
    }

    private String Unzip(byte[] inputByteArr) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(inputByteArr);
            GZIPInputStream gzis = new GZIPInputStream(bais);
            InputStreamReader reader = new InputStreamReader(gzis);
            BufferedReader in = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();

            String line;
            while((line = in.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (Exception var8) {
            return new String(inputByteArr);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wvContent.destroy();
        System.out.println("error");
    }

    class myWebClient extends WebViewClient {
        private final int PAGE_REDIRECTED = 2;
        private final int PAGE_STARTED = 1;

        myWebClient() {
        }

        public void onPageFinished(WebView view, String url) {
//            CustomWebView.this.llLoading.setVisibility(View.GONE);
            super.onPageFinished(view, url);
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            CustomWebView.this.llLoading.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

//        @TargetApi(23)
//        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//            super.onReceivedError(view, request, error);
//            CustomWebView.this.llLoading.setVisibility(View.GONE);
//        }
//
//        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            super.onReceivedError(view, errorCode, description, failingUrl);
//            CustomWebView.this.llLoading.setVisibility(View.GONE);
//        }
//
//        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//            CustomWebView.this.llLoading.setVisibility(View.GONE);
//            super.onReceivedHttpError(view, request, errorResponse);
//        }
//
//        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            CustomWebView.this.llLoading.setVisibility(View.GONE);
//            super.onReceivedSslError(view, handler, error);
//        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            CustomWebView.this.llLoading.setVisibility(View.VISIBLE);
            Intent intent;
            Uri uri;
            if (!TextUtils.isEmpty(url) && url.startsWith("intent://")) {
                try {
                    uri = Uri.parse(url);
                    if (!TextUtils.isEmpty(CustomWebView.this.scheme)) {
                        uri = CustomWebView.addUriParameter(uri, "newcallbackurl", CustomWebView.this.scheme);
                    }

                    new Intent();
                    intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        boolean authenticate = false;

                        try {
                            if (CustomWebView.this.entity_Response == null) {
                                authenticate = true;
                            } else {
                                VNP_BankEntity[] var6 = CustomWebView.this.entity_Response;
                                int var7 = var6.length;

                                for(int var8 = 0; var8 < var7; ++var8) {
                                    VNP_BankEntity bankEntity = var6[var8];
                                    if (bankEntity.andr_scheme.equals(intent.getScheme())) {
                                        authenticate = true;
                                        break;
                                    }
                                }
                            }
                        } catch (Exception var12) {
                            var12.printStackTrace();
                            Log.wtf("SDK", var12.getMessage());
                            authenticate = true;
                        }

                        if (authenticate) {
                            view.stopLoading();

                            try {
                                CustomWebView.this.startActivity(intent);
                                if (CustomWebView.sdkCompletedCallback != null) {
                                    CustomWebView.sdkCompletedCallback.sdkAction("CallMobileBankingApp");
                                }

                                CustomWebView.this.finish();
                            } catch (Exception var11) {
                                String appPackageName = intent.getPackage();

                                try {
                                    CustomWebView.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + appPackageName)));
                                } catch (ActivityNotFoundException var10) {
                                    CustomWebView.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        } else {
                            Toast.makeText(CustomWebView.this, "This bank is not support", Toast.LENGTH_LONG).show();
                        }

                        return true;
                    }
                } catch (Exception var13) {
                    var13.printStackTrace();
                    Log.wtf("SDK", var13);
                }

                return true;
            } else if (url.contains("cancel.sdk.merchantbackapp")) {
                if (CustomWebView.sdkCompletedCallback != null) {
                    CustomWebView.sdkCompletedCallback.sdkAction("WebBackAction");
                }

                CustomWebView.this.finish();
                return true;
            } else if (url.contains("fail.sdk.merchantbackapp")) {
                if (CustomWebView.sdkCompletedCallback != null) {
                    CustomWebView.sdkCompletedCallback.sdkAction("FaildBackAction");
                }

                CustomWebView.this.finish();
                return true;
            } else if (url.contains("success.sdk.merchantbackapp")) {
                if (CustomWebView.sdkCompletedCallback != null) {
                    CustomWebView.sdkCompletedCallback.sdkAction("SuccessBackAction");
                }

                CustomWebView.this.finish();
                return true;
            } else if (url.startsWith("tel:")) {
                intent = new Intent("android.intent.action.DIAL", Uri.parse(url));
                CustomWebView.this.startActivity(intent);
                return true;
            } else if (url.startsWith("mailto:")) {
                Intent i = new Intent("android.intent.action.SENDTO", Uri.parse(url));
                CustomWebView.this.startActivity(i);
                return true;
            } else if (!url.startsWith("http")) {
                uri = Uri.parse(url);
                Intent ix = new Intent("android.intent.action.VIEW", uri);
                CustomWebView.this.startActivity(ix);
                CustomWebView.this.finish();
                return true;
            } else {
                view.loadUrl(url);
                return true;
            }
        }
    }
}
