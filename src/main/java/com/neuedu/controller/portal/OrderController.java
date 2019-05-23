package com.neuedu.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.neuedu.common.Constant;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Order;
import com.neuedu.pojo.User;
import com.neuedu.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    IOrderService orderService;

    /**
     * 创建订单
     * */
    @RequestMapping(value = "/create.do")
    public ServerResponse createOrder(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("用户未登录");
        }
        return orderService.createOrder(user.getId(),shippingId);
    }

    /**
     * 取消订单
     * */
    @RequestMapping(value = "/cancel.do")
    public ServerResponse cancel(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("用户未登录");
        }
        return orderService.cancel(user.getId(),orderNo);
    }


    /**
     * 获取订单的商品信息
     * */
    @RequestMapping(value = "/get_order_cart_product.do")
    public ServerResponse get_order_cart_product(HttpSession session){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("用户未登录");
        }
        return orderService.get_order_cart_product(user.getId());
    }

    /**
     * 订单列表
     * */
    @RequestMapping(value = "/list.do")
    public ServerResponse list(HttpSession session,
                               @RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize",required = false,defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("用户未登录");
        }
        return orderService.list(user.getId(),pageNum,pageSize);
    }


    /**
     * 订单详情detail
     *
     * */
    @RequestMapping(value = "/detail.do")
    public ServerResponse detail(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("用户未登录");
        }
        return orderService.detail(orderNo);
    }


    /**
     * 预下单接口
     *
     * */
    @RequestMapping(value = "pay/{orderNo}")
    public ServerResponse pay(@PathVariable("orderNo") Long orderNo,HttpSession session){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("用户未登录");
        }
        return orderService.pay(user.getId(),orderNo);
    }


    /**
     * 支付宝服务器回调商家服务器接口
     *
     * */
    @RequestMapping("callback.do")
    public String alipay_callback(HttpServletRequest request){
        Map<String, String[]> callbackParams = request.getParameterMap();
        Map<String,String> signParams = Maps.newHashMap();
        Iterator<String> iterator = callbackParams.keySet().iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            String[] values = callbackParams.get(key);
            StringBuffer stringBuffer = new StringBuffer();

            if (values!=null&&values.length>0){
                for (int i=0;i<values.length;i++){
                    stringBuffer.append(values[i]);
                    if (i!=values.length-1){
                        stringBuffer.append(",");
                    }
                }
            }
            signParams.put(key,stringBuffer.toString());

        }

        System.out.println(signParams);
        //验证签名
        try {
            signParams.remove("sign_type");
            boolean result = AlipaySignature.rsaCheckV2(signParams,Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());

            if (result){
                //验签通过
                System.out.println("成功");
                return orderService.callback(signParams);

            }else {
                return "fail";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        return "success";
    }
}
