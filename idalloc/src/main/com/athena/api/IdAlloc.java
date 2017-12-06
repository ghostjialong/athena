package com.athena.api;

import com.athena.idalloc.Manager;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


/**
 * Created by wangjialong on 12/6/17.
 */

@RestController
public class IdAlloc extends BaseApi {

    @Autowired
    private  Manager allocator;

    @RequestMapping(value = "/id", method= RequestMethod.POST)
    public ResponseBody<Body> idAllocForRequest(@RequestParam(value="token") String token) {
        // 1. 生成requestId

        //2. 校验request token

        //3. 分配自增id
        Long id = allocator.idAlloc();

        ResponseBody<Body> responseBody = new ResponseBody();
        responseBody.setData(new Body(id));
        return responseBody;
    }


    public class Body {

        private String requestId = UUID.randomUUID().toString();
        private Long idAssigned;

        Body(Long idAssigned) {
            this.idAssigned = idAssigned;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setIdAssigned(Long idAssigned) {
            this.idAssigned = idAssigned;
        }

        public Long getIdAssigned() {
            return idAssigned;
        }
    }
}
