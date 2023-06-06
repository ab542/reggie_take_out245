package com.itheima.reggie.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;

import java.util.HashMap;

/**
 * 短信发送工具类
 */
public class SMSUtils {

	/**
	 * 发送短信
	 * @param signName 签名
	 * @param templateCode 模板
	 * @param phoneNumbers 手机号
	 * @param param 参数
	 */
	public static void sendMessage(String signName, String templateCode,String phoneNumbers,String param){
		/*用户登录名称 lisa@1166368758826333.onaliyun.com
		AccessKey ID LTAI5t93LNGH9gsSVshJQ8CC
		AccessKey Secret 3weEtHXkGubEqF7J6dHGhsZgS34iPi*/
		/*DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI5t93LNGH9gsSVshJQ8CC", "3weEtHXkGubEqF7J6dHGhsZgS34iPi");
		IAcsClient client = new DefaultAcsClient(profile);

		SendSmsRequest request = new SendSmsRequest();
		request.setSysRegionId("cn-hangzhou");
		request.setPhoneNumbers(phoneNumbers);
		request.setSignName(signName);
		request.setTemplateCode(templateCode);
		request.setTemplateParam("{\"code\":\""+param+"\"}");
		try {
			SendSmsResponse response = client.getAcsResponse(request);
			System.out.println("短信发送成功");
		}catch (ClientException e) {
			e.printStackTrace();
		}*/
		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI5t93LNGH9gsSVshJQ8CC", "3weEtHXkGubEqF7J6dHGhsZgS34iPi");
		IAcsClient client = new DefaultAcsClient(profile);

		CommonRequest request = new CommonRequest();
		request.setSysMethod(MethodType.POST);
		request.setSysDomain("lisa@1166368758826333.onaliyun.com");
		request.setSysVersion("2017-05-25");
		request.setSysAction("SendSms");
		request.putQueryParameter("RegionId", "cn-hangzhou");
		request.putQueryParameter("PhoneNumbers", phoneNumbers);
		request.putQueryParameter("SignName", signName);
		request.putQueryParameter("TemplateCode", templateCode);
		request.putQueryParameter("TemplateParam", "{\"code\":\""+param+"\"}");
		try {
			CommonResponse response = client.getCommonResponse(request);
			System.out.println(response.getData());
			String json = response.getData();
			Gson g = new Gson();
			HashMap result = g.fromJson(json, HashMap.class);
			if("OK".equals(result.get("Message"))) {
				System.out.println("短信发送成功");
			}else{
				System.out.println("短信发送失败，原因："+result.get("Message"));
			}
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			e.printStackTrace();
		}


	}

	public void send(){
		DefaultProfile profile = DefaultProfile.getProfile("ap-southeast-1", "LTAI5t93LNGH9gsSVshJQ8CC", "3weEtHXkGubEqF7J6dHGhsZgS34iPi");
		/** use STS Token
		 DefaultProfile profile = DefaultProfile.getProfile(
		 "<your-region-id>",           // The region ID
		 "<your-access-key-id>",       // The AccessKey ID of the RAM account
		 "<your-access-key-secret>",   // The AccessKey Secret of the RAM account
		 "<your-sts-token>");          // STS Token
		 **/

		IAcsClient client = new DefaultAcsClient(profile);

		SendSmsRequest request = new SendSmsRequest();

		try {
			SendSmsResponse response = client.getAcsResponse(request);
			System.out.println(new Gson().toJson(response));
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			System.out.println("ErrCode:" + e.getErrCode());
			System.out.println("ErrMsg:" + e.getErrMsg());
			System.out.println("RequestId:" + e.getRequestId());
		}
	}

}
