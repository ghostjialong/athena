package com.athena.idalloc.api;


/**
 * Created by wangjialong on 12/30/17.
 */
public interface IdRequest extends java.io.Serializable {

    BaseApi.ResponseBody<IdAlloc.Body> idAllocForRequest(String token);
}
