package com.mycompany.plugins.example;

import android.content.Intent;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.NativePlugin;
import com.vnpay.authentication.VNP_AuthenticationActivity;
import com.vnpay.authentication.VNP_SdkCompletedCallback;
//import com.getcapacitor.annotation.CapacitorPlugin;

//@CapacitorPlugin(name = "VNPayIntegrated")
@NativePlugin(
        requestCodes = {123123}
)
public class VNPayIntegratedPlugin extends Plugin {

    private VNPayIntegrated implementation = new VNPayIntegrated();

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");
        openSdk(call);
        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    public void openSdk(PluginCall call) {
        Intent intent = new Intent(this.getContext(), VNP_AuthenticationActivity.class);
        intent.putExtra("url", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=10000000&vnp_Command=pay&vnp_CreateDate=20230214163402&vnp_CurrCode=VND&vnp_ExpireDate=20230214164902&vnp_IpAddr=192.168.1.1&vnp_Locale=vi&vnp_OrderInfo=Thanh+toan+don+hang%3A64162145&vnp_OrderType=25000&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fvnpay_jsp%2Fvnpay_return.jsp&vnp_TmnCode=CATHAYAP&vnp_TxnRef=64162145&vnp_Version=2.1.0&vnp_SecureHash=3fc317c6b446c48d4dc0623aeadfbff068de1a357cc0f7098abbe23a0c3e9631b53026843278200e1dac5a48e7c3ab873f8322bd793ff9d093080f5e260f18c4"); //bắt buộc, VNPAY cung cấp
        intent.putExtra("tmn_code", "CATHAYAP"); //bắt buộc, VNPAY cung cấp
        intent.putExtra("scheme", "resultactivity"); //bắt buộc, scheme để mở lại app khi có kết quả thanh toán từ mobile banking
        intent.putExtra("is_sandbox", true); //bắt buộc, true <=> môi trường test, true <=> môi trường live
        VNP_AuthenticationActivity.setSdkCompletedCallback(new VNP_SdkCompletedCallback() {
            @Override
            public void sdkAction(String action) {
                JSObject ret = new JSObject();
                ret.put("value", action);
                call.resolve(ret);
                Log.wtf("SplashActivity", "action: " + action);
                if( action == "AppBackAction"){
                    Log.wtf("AppBackAction","action" + action);
                }

                //action == AppBackAction
                //Người dùng nhấn back từ sdk để quay lại

                //action == CallMobileBankingApp
                //Người dùng nhấn chọn thanh toán qua app thanh toán (Mobile Banking, Ví...)
                //lúc này app tích hợp sẽ cần lưu lại cái PNR, khi nào người dùng mở lại app tích hợp thì sẽ gọi kiểm tra trạng thái thanh toán của PNR Đó xem đã thanh toán hay chưa.

                //action == WebBackAction
                //Người dùng nhấn back từ trang thanh toán thành công khi thanh toán qua thẻ khi url có chứa: cancel.sdk.merchantbackapp

                //action == FaildBackAction
                //giao dịch thanh toán bị failed

                //action == SuccessBackAction
                //thanh toán thành công trên webview
            }
        });
        this.getActivity().startActivity(intent);
    }
}
