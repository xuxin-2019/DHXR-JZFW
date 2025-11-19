package com.homemaker.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 微信支付工具类
 */
@Component
public class WechatPayUtil {

    @Value("${wechat.pay.appid}")
    private String appid;

    @Value("${wechat.pay.mchid}")
    private String mchid;

    @Value("${wechat.pay.key}")
    private String key;

    @Value("${wechat.pay.notify_url}")
    private String notifyUrl;

    @Value("${wechat.pay.refund_notify_url}")
    private String refundNotifyUrl;

    // 微信支付API地址
    private static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    private static final String ORDER_QUERY_URL = "https://api.mch.weixin.qq.com/pay/orderquery";
    private static final String CLOSE_ORDER_URL = "https://api.mch.weixin.qq.com/pay/closeorder";
    private static final String REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
    private static final String REFUND_QUERY_URL = "https://api.mch.weixin.qq.com/pay/refundquery";

    /**
     * 生成签名
     * @param params 参数字典
     * @return 签名结果
     */
    public String generateSign(Map<String, String> params) {
        // 1. 参数排序
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        // 2. 拼接字符串
        StringBuilder sb = new StringBuilder();
        for (String k : keys) {
            String v = params.get(k);
            if (v != null && !v.isEmpty() && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k).append("=").append(v).append("&");
            }
        }
        // 3. 拼接API密钥
        sb.append("key=").append(key);

        // 4. MD5加密
        return MD5(sb.toString()).toUpperCase();
    }

    /**
     * MD5加密
     * @param str 待加密字符串
     * @return 加密结果
     */
    private String MD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes("UTF-8"));
            byte[] byteDigest = md.digest();
            int i;
            StringBuilder buf = new StringBuilder(32);
            for (byte b : byteDigest) {
                i = b;
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 字典转XML
     * @param params 参数字典
     * @return XML字符串
     */
    public String mapToXml(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("<").append(entry.getKey()).append(">");
            sb.append("<![CDATA[")
              .append(entry.getValue() != null ? entry.getValue() : "")
              .append("]]>");
            sb.append("</").append(entry.getKey()).append(">");
        }
        sb.append("</root>");
        return sb.toString();
    }

    /**
     * XML转字典
     * @param xml XML字符串
     * @return 参数字典
     */
    public Map<String, String> xmlToMap(String xml) {
        Map<String, String> map = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            org.w3c.dom.Document document = builder.parse(is);

            org.w3c.dom.Element root = document.getDocumentElement();
            org.w3c.dom.NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    map.put(element.getNodeName(), element.getTextContent());
                }
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 发送HTTP POST请求
     * @param url 请求地址
     * @param data 请求数据
     * @param useSSL 是否使用SSL
     * @return 响应结果
     */
    private String httpRequest(String url, String data, boolean useSSL) {
        try {
            CloseableHttpClient httpClient;
            if (useSSL) {
                // 使用SSL的请求（如退款接口）
                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                        builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            } else {
                // 普通请求
                httpClient = HttpClients.createDefault();
            }

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(data, "UTF-8"));
            httpPost.setHeader("Content-Type", "text/xml");

            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");

            response.close();
            httpClient.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成随机字符串
     * @return 随机字符串
     */
    public String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成时间戳
     * @return 时间戳
     */
    public String generateTimeStamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    /**
     * 统一下单
     * @param outTradeNo 商户订单号
     * @param totalFee 总金额（分）
     * @param body 商品描述
     * @param openid 用户openid
     * @return 下单结果
     */
    public Map<String, String> unifiedOrder(String outTradeNo, Integer totalFee, String body, String openid) {
        Map<String, String> params = new HashMap<>();
        params.put("appid", appid);
        params.put("mch_id", mchid);
        params.put("nonce_str", generateNonceStr());
        params.put("body", body);
        params.put("out_trade_no", outTradeNo);
        params.put("total_fee", String.valueOf(totalFee));
        params.put("spbill_create_ip", "127.0.0.1"); // 实际应用中需要获取真实IP
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "JSAPI");
        params.put("openid", openid);

        // 生成签名
        String sign = generateSign(params);
        params.put("sign", sign);

        // 转换为XML
        String xml = mapToXml(params);

        // 发送请求
        String responseXml = httpRequest(UNIFIED_ORDER_URL, xml, false);
        if (responseXml == null) {
            return null;
        }

        // 解析响应
        return xmlToMap(responseXml);
    }

    /**
     * 查询订单
     * @param outTradeNo 商户订单号
     * @return 查询结果
     */
    public Map<String, String> orderQuery(String outTradeNo) {
        Map<String, String> params = new HashMap<>();
        params.put("appid", appid);
        params.put("mch_id", mchid);
        params.put("out_trade_no", outTradeNo);
        params.put("nonce_str", generateNonceStr());

        // 生成签名
        String sign = generateSign(params);
        params.put("sign", sign);

        // 转换为XML
        String xml = mapToXml(params);

        // 发送请求
        String responseXml = httpRequest(ORDER_QUERY_URL, xml, false);
        if (responseXml == null) {
            return null;
        }

        // 解析响应
        return xmlToMap(responseXml);
    }

    /**
     * 关闭订单
     * @param outTradeNo 商户订单号
     * @return 关闭结果
     */
    public Map<String, String> closeOrder(String outTradeNo) {
        Map<String, String> params = new HashMap<>();
        params.put("appid", appid);
        params.put("mch_id", mchid);
        params.put("out_trade_no", outTradeNo);
        params.put("nonce_str", generateNonceStr());

        // 生成签名
        String sign = generateSign(params);
        params.put("sign", sign);

        // 转换为XML
        String xml = mapToXml(params);

        // 发送请求
        String responseXml = httpRequest(CLOSE_ORDER_URL, xml, false);
        if (responseXml == null) {
            return null;
        }

        // 解析响应
        return xmlToMap(responseXml);
    }

    /**
     * 申请退款
     * @param transactionId 微信支付订单号
     * @param outTradeNo 商户订单号
     * @param outRefundNo 商户退款单号
     * @param totalFee 订单总金额（分）
     * @param refundFee 退款金额（分）
     * @param refundDesc 退款原因
     * @return 退款结果
     */
    public Map<String, String> refund(String transactionId, String outTradeNo, String outRefundNo, 
                                     Integer totalFee, Integer refundFee, String refundDesc) {
        Map<String, String> params = new HashMap<>();
        params.put("appid", appid);
        params.put("mch_id", mchid);
        params.put("nonce_str", generateNonceStr());
        params.put("transaction_id", transactionId);
        params.put("out_trade_no", outTradeNo);
        params.put("out_refund_no", outRefundNo);
        params.put("total_fee", String.valueOf(totalFee));
        params.put("refund_fee", String.valueOf(refundFee));
        params.put("refund_desc", refundDesc);
        params.put("notify_url", refundNotifyUrl);

        // 生成签名
        String sign = generateSign(params);
        params.put("sign", sign);

        // 转换为XML
        String xml = mapToXml(params);

        // 发送请求（退款接口需要SSL）
        String responseXml = httpRequest(REFUND_URL, xml, true);
        if (responseXml == null) {
            return null;
        }

        // 解析响应
        return xmlToMap(responseXml);
    }

    /**
     * 查询退款
     * @param outRefundNo 商户退款单号
     * @return 查询结果
     */
    public Map<String, String> refundQuery(String outRefundNo) {
        Map<String, String> params = new HashMap<>();
        params.put("appid", appid);
        params.put("mch_id", mchid);
        params.put("out_refund_no", outRefundNo);
        params.put("nonce_str", generateNonceStr());

        // 生成签名
        String sign = generateSign(params);
        params.put("sign", sign);

        // 转换为XML
        String xml = mapToXml(params);

        // 发送请求
        String responseXml = httpRequest(REFUND_QUERY_URL, xml, false);
        if (responseXml == null) {
            return null;
        }

        // 解析响应
        return xmlToMap(responseXml);
    }

    /**
     * 生成JSAPI支付参数
     * @param prepayId 预支付ID
     * @return JSAPI支付参数
     */
    public Map<String, String> generateJsapiParams(String prepayId) {
        Map<String, String> params = new HashMap<>();
        params.put("appId", appid);
        params.put("timeStamp", generateTimeStamp());
        params.put("nonceStr", generateNonceStr());
        params.put("package", "prepay_id=" + prepayId);
        params.put("signType", "MD5");

        // 生成签名
        String sign = generateSign(params);
        params.put("paySign", sign);

        return params;
    }

    /**
     * 验证回调签名
     * @param params 回调参数
     * @return 是否验证通过
     */
    public boolean verifySign(Map<String, String> params) {
        if (!params.containsKey("sign")) {
            return false;
        }
        String sign = params.get("sign");
        params.remove("sign");
        String newSign = generateSign(params);
        return sign.equals(newSign);
    }

    /**
     * 将金额转换为分
     * @param amount 金额（元）
     * @return 金额（分）
     */
    public Integer amountToFen(BigDecimal amount) {
        return amount.multiply(new BigDecimal(100)).intValue();
    }

    /**
     * 将分转换为金额
     * @param amount 金额（分）
     * @return 金额（元）
     */
    public BigDecimal fenToAmount(Integer amount) {
        return new BigDecimal(amount).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
    }
}