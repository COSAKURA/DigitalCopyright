package com.digitalcopyright.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 工具类：用于与 WeBASE 平台交互，发送 HTTP POST 请求调用链上合约方法。
 * @author Sakura
 */
@Service
public class WeBASEUtils {

    // 从配置文件中注入 WeBASE 的 URL 地址
    @Value("${webase.url}")
    private String webaseUrl;

    // 从配置文件中注入合约地址
    @Value("${fisco.contract.address}")
    private String contractAddress;

    // ABI 文件内容（合约的应用二进制接口）
    private static final String ABI = IOUtil.readResourceAsString("abi/DigitalCopyright.abi");

    /**
     * 发送 POST 请求调用链上合约方法
     *
     * @param userAddress 用户地址
     * @param funcName    合约方法名称
     * @param funcParam   合约方法的参数
     * @return 请求结果的 JSON 格式字符串
     */
    public String funcPost(String userAddress, String funcName, List<Object> funcParam) {

        // 验证配置
        if (webaseUrl == null || webaseUrl.isEmpty()) {
            throw new IllegalArgumentException("WeBASE URL 未正确配置，请检查配置文件。");
        }
        if (contractAddress == null || contractAddress.isEmpty()) {
            throw new IllegalArgumentException("合约地址未正确配置，请检查配置文件。");
        }

        // 将合约 ABI 转换为 JSON 数组
        JSONArray abiJSON = JSONUtil.parseArray(ABI);

        // 构建请求数据的 JSON 对象
        JSONObject data = JSONUtil.createObj();
        data.set("groupId", "1"); // 组 ID，默认为 1
        data.set("contractPath", "/"); // 合约路径（与合约部署相关）
        data.set("contractAbi", abiJSON); // 合约的 ABI 描述
        data.set("useAes", false); // 是否使用 AES 加密，false 表示不使用
        data.set("useCns", false); // 是否使用 CNS（合约名称服务），false 表示不使用
        data.set("cnsName", ""); // CNS 名称，如果使用则填写
        data.set("user", userAddress); // 调用合约方法的用户地址
        data.set("contractAddress", contractAddress); // 合约地址
        data.set("funcName", funcName); // 要调用的合约方法名称
        data.set("funcParam", funcParam); // 合约方法所需的参数列表

        // 将 JSON 对象转为字符串格式，作为请求数据
        String dataString = JSONUtil.toJsonStr(data);

        // 创建 HTTP 客户端
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建 HTTP POST 请求对象
            HttpPost httpPost = new HttpPost(webaseUrl);

            // 设置请求头，指定请求内容为 JSON 格式，编码为 UTF-8
            httpPost.setHeader("Content-type", "application/json;charset=utf-8");

            // 设置 POST 请求的消息实体，包含 JSON 数据
            StringEntity entity = new StringEntity(dataString, Charset.forName("UTF-8"));
            // 设置消息实体的编码格式
            entity.setContentEncoding("UTF-8");
            // 设置内容类型为 JSON 格式
            entity.setContentType("application/json");
            // 将消息实体设置到 HTTP POST 请求对象中
            httpPost.setEntity(entity);

            // 执行 HTTP POST 请求
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                // 将响应体转换为字符串，编码为 UTF-8
                return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            }
        } catch (IOException e) {
            // 输出详细错误日志
            System.err.println("HTTP 请求失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("调用 WeBASE 接口失败，请检查网络连接或 WeBASE 配置。", e);
        }
    }
}
