package com.gkzxhn.gkprison.constant;

import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SignUtils;

/**
 * Author: Huang ZN
 * Date: 2016/12/13
 * Email:943852572@qq.com
 * Description:
 */

public class AliPayConst {

    public static final String TAG = "AliPayConst";
    public static final String PARTNER = "2088121417397335";
    public static final String SELLER = "130146668@qq.com";// 商户收款账号
    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE =
            "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAL62E/1KH+LunsXU" +
                    "9shOfFXhg6S7qd9e8b1TieCtVuZpK/maw69rv8oxoqTCXD/oUuuwszY7JIXVX8o0" +
                    "vcnK30rxTM10i5YIk16VTvqvf5D3VKxYfDJlldkiaxlO79L5A8lg224jjAbjWkqm" +
                    "kvIhaoAxXq1ythdBY1KujN9OJnjVAgMBAAECgYEAnihF35KvavVVOt9oYamlN1u0" +
                    "XtM7B4GnnMlA2NEn9iFWVMPicQI8paQQK+77rgwvaEK7/MeDfHH95KVkl4rlLcMG" +
                    "FoUUAgvrRFdS2Xv6RSaci3fvkai7MeHKQQ8j/+1dABjJcQF/OfMPHpCPrK4kxQ5Q" +
                    "sCF132mjUpiwtpzV+ikCQQD1GO6Wx/fSV2+Ihaa9coPR57kI6xpDr48r9utUHVIw" +
                    "w0sTblsGWwgs+to7SG10m6kOqb+vsCTayoFY1cQEA9vzAkEAxzHRrn1uOXzF+V/w" +
                    "FcsQvZrC9oK4ed0Lanc36WiJf8a31X8w7N0PxXzQVHbatm3GrrII2q/0ASntxmfW" +
                    "RxbyFwJBAOzHrk9KVhcF00Ev5Pqmc8TIORDtl80GAKm3fHchcHKdaJ0YAqXsMcTK" +
                    "fyPAf8WkT7lTslR3NdOMyVLaCOjcFZMCQHRUsgJXmoHUTsJetxXjK/mvYmEY4qe4" +
                    "4ivhSDP2KycGZOI4j9glGkrZo8lQSFb2MWxg6S7eR4BOfmC6z7dgvS0CQQDLrn0S" +
                    "+lotPkFBMBCh38KVEEI9Pb71SkRL05kHR4+sesm4a4bh72mrMKbcCXYxJRPXwt7k" +
                    "yQd7PvRoFL2mwp8f";

    /**
     * 签名
     * @param content
     * @return
     */
    public static String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    public static String getSignType() {
        return "sign_type=\"RSA\"";
    }

    /**
     *  create the order info.
     *  创建订单信息
     * @param subject
     * @param body
     * @param price
     * @return
     */
    public static String getOrderInfo(String subject, String body, String price, String TradeNo) {
        String orderInfo = "partner=" + "\"" + AliPayConst.PARTNER + "\"";// 签约合作者身份ID
        orderInfo += "&seller_id=" + "\"" + AliPayConst.SELLER + "\"";// 签约卖家支付宝账号
        orderInfo += "&out_trade_no=" + "\"" + TradeNo + "\"";// 商户网站唯一订单号
        orderInfo += "&subject=" + "\"" + subject + "\"";// 商品名称
        orderInfo += "&body=" + "\"" + body + "\"";// 商品详情
        orderInfo += "&total_fee=" + "\"" + price + "\"";// 商品金额
        orderInfo += "&notify_url=" + "\"" + "https://www.fushuile.com/api/v1/payment" + "\"";// 服务器异步通知页面路径
        orderInfo += "&service=\"mobile.securitypay.pay\"";// 服务接口名称， 固定值
        orderInfo += "&payment_type=\"1\"";// 支付类型， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";// 参数编码， 固定值
        /**
         *  设置未付款交易的超时时间
         *  默认30分钟，一旦超时，该笔交易就会自动被关闭。
         *  取值范围：1m～15d。
         *  m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
         *  该参数数值不接受小数点，如1.5h，可转换为90m。
         */
        orderInfo += "&it_b_pay=\"30m\"";
        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";
        orderInfo += "&return_url=\"m.alipay.com\"";// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";
        Log.d(TAG, "orderInfo: " + orderInfo);
        return orderInfo;
    }
}
