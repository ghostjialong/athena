package com.athena.idalloc.client;

import com.alibaba.dubbo.common.bytecode.Proxy;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.athena.idalloc.api.BaseApi;
import com.athena.idalloc.api.IdAlloc;
import com.athena.idalloc.api.IdRequest;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wangjialong on 12/6/17.
 */
@Service
public class IdallocClient {

    @Reference(interfaceClass=IdRequest.class, generic = true)
    public GenericService idAllocDubbo;

    /*@Reference(interfaceClass=IdRequest.class, interfaceName="com.athena.idalloc.api.IdRequest")
    public IdRequest idAllocDubboWithoutGeneratic;*/

    public static Long requestIdAlloc() throws Exception {
        HttpClient httpClient = HttpClients.custom().build();

        String path = "http://127.0.0.1:8082/id";
        HttpPost httpPost = new HttpPost(path);
        List<NameValuePair> requestBody = new ArrayList();
        requestBody.add(new BasicNameValuePair("token", "MhxzKhl"));
        //httpPost.setC
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(requestBody, "UTF-8");
        entity.setContentType("application/x-www-form-urlencoded");
        httpPost.setEntity(entity);
        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity responseEntity = httpResponse.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent(), "UTF-8"));
        String line = reader.readLine();
        JSONObject jsonObject = new JSONObject(line);
        JSONObject data = (JSONObject)jsonObject.get("data");
        Long id = Long.valueOf((Integer)data.get("idAssigned"));
        return id;
    }

    public Long requestIdAllocDubbo() {
        HashMap result = (HashMap) this.idAllocDubbo.$invoke("idAllocForRequest", new String[] {"java.lang.String"},
                new Object[] {"MhxzKhl"});
        HashMap body = (HashMap) result.get("data");
        Long id = (Long) body.get("idAssigned");
        return id;
    }

    /*public Long requestIdAllocDubboTest() {
        BaseApi.ResponseBody<IdAlloc.Body> result =
                 idAllocDubboWithoutGeneratic.idAllocForRequest("mHXZkHL");
        Long id = result.getData().getIdAssigned();
        return id;
    }*/
}
