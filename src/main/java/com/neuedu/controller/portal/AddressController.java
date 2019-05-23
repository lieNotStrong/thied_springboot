package com.neuedu.controller.portal;

import com.neuedu.common.Constant;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Shipping;
import com.neuedu.pojo.User;
import com.neuedu.service.IAddressService;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/shipping")
public class AddressController {

    @Autowired
    IAddressService addressService;

    /**
     * 添加地址
     * */
    @RequestMapping(value = "/add.do")
    public ServerResponse add(HttpSession session, Shipping shipping){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);

        if (user==null){
            return ServerResponse.serverResponseByError("需要登录");
        }
        return addressService.add(user.getId(),shipping);
    }

    /**
     * 删除地址
     * */
    @RequestMapping(value = "/del.do")
    public ServerResponse del(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);

        if (user==null){
            return ServerResponse.serverResponseByError("需要登录");
        }
        return addressService.del(user.getId(),shippingId);
    }

    /**
     * 登录状态下更新地址
     * */
    @RequestMapping(value = "/update.do")
    public ServerResponse update(HttpSession session, Shipping shipping){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);

        if (user==null){
            return ServerResponse.serverResponseByError("需要登录");
        }
        shipping.setUserId(user.getId());
        return addressService.update(shipping);
    }

    /**
     * 选中查看具体地址
     * */
    @RequestMapping(value = "/select.do")
    public ServerResponse select(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);

        if (user==null){
            return ServerResponse.serverResponseByError("需要登录");
        }

        return addressService.select(shippingId);
    }

    /**
     * 分页查询
     * */
    @RequestMapping(value = "/list.do")
    public ServerResponse list(HttpSession session,
                               @RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize",required = false,defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);

        if (user==null){
            return ServerResponse.serverResponseByError("需要登录");
        }

        return addressService.list(pageNum,pageSize);
    }

}
