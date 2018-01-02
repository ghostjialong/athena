package com.athena.idalloc.api;

/**
 * Created by wangjialong on 12/6/17.
 */
public class BaseApi {

    public void setMessage(String test) {

    }

    public class ResponseBody <T> implements java.io.Serializable {

        private String code = "0";

        private String message = "";

        private T data;

        public void setCode(String code) {
                this.code = code;
            }

        public void setMessage(String message) {
                this.message = message;
            }

        public String getCode() {
                return this.code;
            }

        public String getMessage() {
                return this.message;
            }

        public void setData(T data) {
                this.data = data;
            }

        public T getData() {
                return data;
            }
    }

}
