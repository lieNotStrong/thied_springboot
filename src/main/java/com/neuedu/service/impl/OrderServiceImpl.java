package com.neuedu.service.impl;

import com.alibaba.druid.sql.visitor.functions.If;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayMonitorService;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayMonitorServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeWithHBServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.neuedu.alipay.Main;
import com.neuedu.common.Constant;
import com.neuedu.common.EnumClass;
import com.neuedu.common.GoodsStates;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.*;
import com.neuedu.pojo.*;
import com.neuedu.service.IOrderService;
import com.neuedu.utils.BigDecimalUtils;
import com.neuedu.utils.DateUtils;
import com.neuedu.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    CartMapper cartMapper;
    @Autowired
    ProductMapper productMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    ShippingMapper shippingMapper;

    @Autowired
    PayInfoMapper payInfoMapper;

    @Value("${thied_springboot.imageHost}")
    private String imageHost;
    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {

        //step1:参数的非空判断
        if (shippingId==null){
            return ServerResponse.serverResponseByError("地址id不能为空");
        }

        //step2：根据userid查询购物车中已选中的商品-->List<Cart>

        List<Cart> cartList = cartMapper.findCartListByUserIdAndChecked(userId);
        //step3:List<Cart>-->List<OrderItem>
        ServerResponse serverResponse = getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()){
            return serverResponse;
        }
        //step4:创建订单order并将其保存到数据库
        //计算订单的价格
        BigDecimal orderTotalPrice = new BigDecimal("0");
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getDate();
        if (orderItemList==null||orderItemList.size()==0){
            return ServerResponse.serverResponseByError("购物车为空");
        }
        orderTotalPrice = getOrderPrice(orderItemList);
        Order order = create(userId, shippingId, orderTotalPrice);
        if (order==null){
            return ServerResponse.serverResponseByError("创建订单失败");
        }
        //step5:将List<OrderItem>保存到数据库
        for (OrderItem orderItem:orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        //批量插入
        orderItemMapper.insertBatch(orderItemList);
        //setp6：扣库存
        reduceProductStoctStock(orderItemList);
        //step7：购物车中清空已下单的商品
        cleanCart(cartList);
        //step8：返回OrderVO
        OrderVO orderVO = assmbleOrderVO(order, orderItemList, shippingId);

        return ServerResponse.serverResponseBySuccess(orderVO);
    }

    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {

        //step1:参数的非空判断
        if (orderNo==null||orderNo.equals("")){
            return ServerResponse.serverResponseByError("参数不能为空");
        }
        //step2：根据userid和orderno查询订单
        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId, orderNo);
        if (order==null){
            return ServerResponse.serverResponseByError("订单不存在");
        }
        //step3：判断订单状态并取消
        if (order.getStatus()!=EnumClass.OrderStatusEnum.ORDER_UN_PAY.getStatus()){
            return ServerResponse.serverResponseByError("订单不可取消");
        }
        //step4：返回结果
        order.setStatus(EnumClass.OrderStatusEnum.ORDER_CANCELED.getStatus());
        int result = orderMapper.updateByPrimaryKey(order);
        if (result>0){
            return ServerResponse.serverResponseBySuccess();
        }
        return ServerResponse.serverResponseByError("取消订单失败");
    }

    @Override
    public ServerResponse get_order_cart_product(Integer userId) {


        //step1：查询购物车
        List<Cart> cartList = cartMapper.findCartListByUserIdAndChecked(userId);

        //step2：List<Cart>-->List<OrderItem>
        ServerResponse serverResponse = getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()){
            return serverResponse;
        }
        //step3:组装vo
        CartOrderItemVO cartOrderItemVO = new CartOrderItemVO();
        cartOrderItemVO.setImageHost(imageHost);
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getDate();
        List<OrderItemVO> orderItemVOList = Lists.newArrayList();
        if (orderItemList==null||orderItemList.size()==0){
            return ServerResponse.serverResponseByError("购物车空");
        }
        for (OrderItem orderItem:orderItemList){
            orderItemVOList.add(assembleOrderItemVO(orderItem));
        }
        cartOrderItemVO.setOrderItemVOList(orderItemVOList);

        cartOrderItemVO.setTotalPrice(getOrderPrice(orderItemList));

        return ServerResponse.serverResponseBySuccess(cartOrderItemVO);
    }

    @Override
    public ServerResponse list(Integer userId,Integer pageNum,Integer pageSize) {

        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = Lists.newArrayList();
        if (userId==null){
            //查询所有
            orderList = orderMapper.selectAll();
        }else {
            //查询当前用户
            orderList = orderMapper.findOrderByUserId(userId);
        }

        if (orderList==null||orderList.size()==0){
            return ServerResponse.serverResponseByError("未查询到订单信息");
        }
        List<OrderVO> orderVOList = Lists.newArrayList();
        for (Order order:orderList){
            List<OrderItem> orderItemList = orderItemMapper.findOrderItemByOrderNo(order.getOrderNo());
            OrderVO orderVO = assmbleOrderVO(order, orderItemList, order.getShippingId());

            orderVOList.add(orderVO);
        }

        PageInfo pageInfo = new PageInfo(orderVOList);
        return ServerResponse.serverResponseBySuccess(pageInfo);
    }

    @Override
    public ServerResponse detail(Long orderNo) {

        //step1:参数的非空校验
        if (orderNo==null){
            return ServerResponse.serverResponseByError("参数不能为空");
        }
        //step2：查询订单
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order==null){
            return ServerResponse.serverResponseByError("订单不存在");
        }
        //step3：获取ordervo
        List<OrderItem> orderItemList = orderItemMapper.findOrderItemByOrderNo(order.getOrderNo());
        OrderVO orderVO = assmbleOrderVO(order, orderItemList, order.getShippingId());
        //step4：返回结果
        return ServerResponse.serverResponseBySuccess(orderVO);


    }

    @Override
    public ServerResponse pay(Integer userId, Long orderNo) {


        if (orderNo==null){
            return ServerResponse.serverResponseByError(Constant.ERROR,"订单号必须传");
        }
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order==null){
            return ServerResponse.serverResponseByError(Constant.ERROR,"传递的订单不存在");
        }
        return pay(order);
    }

    @Override
    public String callback(Map<String, String> requestParams) {

        //step1:获取各个参数
        //订单号
        String orderNo = requestParams.get("out_trade_no");
        //流水号
        String trade_no = requestParams.get("trade_no");
        //支付状态
        String trade_status = requestParams.get("trade_status");
        //付款时间
        String payment_time = requestParams.get("gmt_payment");

        //step2：根据订单号查询订单
        Order order = orderMapper.selectOrderByOrderNo(Long.parseLong(orderNo));
        if (order==null){
            return "fail";
        }

        if (trade_status.equals("TRADE_SUCCESS")){
            //支付成功
            //修改订单状态
            Order order1 = new Order();
            order1.setOrderNo(Long.parseLong(orderNo));
            order1.setStatus(EnumClass.OrderStatusEnum.ORDER_PAYED.getStatus());
            order1.setPaymentTime(DateUtils.strToDate(payment_time));
            int result = orderMapper.updateOrderStatusAndPaymentTimeByOrderNo(order1);
            if (result<=0){
                return "fail";
            }
        }


        //添加支付记录
        PayInfo payInfo = new PayInfo();
        payInfo.setOrderNo(Long.parseLong(orderNo));
        payInfo.setUserId(order.getUserId());
        payInfo.setPayPlatform(EnumClass.PaymentEnum.ONLINE.getStatus());
        payInfo.setPlatformNumber(trade_no);
        payInfo.setPlatformStatus(trade_status);

        int result = payInfoMapper.insert(payInfo);
        if (result<=0){
            return "fail";
        }

        return "success";
    }


    private static Log log = LogFactory.getLog(Main.class);

    // 支付宝当面付2.0服务
    private static AlipayTradeService tradeService;

    // 支付宝当面付2.0服务（集成了交易保障接口逻辑）
    private static AlipayTradeService   tradeWithHBService;

    // 支付宝交易保障接口服务，供测试接口api使用，请先阅读readme.txt
    private static AlipayMonitorService monitorService;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        // 支付宝当面付2.0服务（集成了交易保障接口逻辑）
        tradeWithHBService = new AlipayTradeWithHBServiceImpl.ClientBuilder().build();

        /** 如果需要在程序中覆盖Configs提供的默认参数, 可以使用ClientBuilder类的setXXX方法修改默认参数 否则使用代码中的默认设置 */
        monitorService = new AlipayMonitorServiceImpl.ClientBuilder()
                .setGatewayUrl("http://mcloudmonitor.com/gateway.do").setCharset("GBK")
                .setFormat("json").build();
    }

    // 测试当面付2.0生成支付二维码
    public ServerResponse  pay(Order order) {
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo =String.valueOf(order.getOrderNo());

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = "【睿乐购】平台商品支付";

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = "购买商品共"+order.getPayment()+"元";

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "teost_operatr_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        //根据orderNo查询订单明细
        List<OrderItem> orderItemList= orderItemMapper.findOrderItemByOrderNo(order.getOrderNo());
        if(orderItemList==null||orderItemList.size()==0){
            return ServerResponse.serverResponseByError(Constant.ERROR,"没有可购买的商品");
        }

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        for(OrderItem orderItem:orderItemList){
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(String.valueOf(orderItem.getProductId()), orderItem.getProductName(), orderItem.getCurrentUnitPrice().intValue(),
                    orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(" http://44ttxw.natappfree.cc/order/callback.do")//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                // 需要修改为运行机器上的路径
                String filePath = String.format("e:/upload/qr-%s.png",
                        response.getOutTradeNo());
                log.info("filePath:" + filePath);
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                PayVO payVO=new PayVO(order.getOrderNo(),imageHost+"qr-"+response.getOutTradeNo()+".png");
                return ServerResponse.serverResponseBySuccess(payVO);

            case FAILED:
                log.error("支付宝预下单失败!!!");
                break;

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                break;

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                break;
        }
        return  ServerResponse.serverResponseByError();
    }
    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }





    private OrderVO assmbleOrderVO (Order order, List<OrderItem> orderItemList, Integer shippingId){
        OrderVO orderVO = new OrderVO();
        List<OrderItemVO> orderItemVOList = Lists.newArrayList();
        for (OrderItem orderItem:orderItemList){
            OrderItemVO orderItemVO = assembleOrderItemVO(orderItem);
            orderItemVOList.add(orderItemVO);
        }

        orderVO.setOrderItemVOList(orderItemVOList);
        orderVO.setImageHost(imageHost);
        Shipping shipping = shippingMapper.selectByPrimaryKey(shippingId);
        if (shipping!=null){
            orderVO.setShippingId(shippingId);
            ShippingVO shippingVO = assmbleShippingVO(shipping);
            orderVO.setShippingVO(shippingVO);
            orderVO.setReceiverName(shipping.getReceiverName());
        }

        orderVO.setStatus(order.getStatus());
        EnumClass.OrderStatusEnum orderStatusEnum = EnumClass.OrderStatusEnum.codeOf(order.getStatus());
        if (orderStatusEnum!=null){
            orderVO.setStatusDesc(orderStatusEnum.getDesc());
        }
        orderVO.setPostage(0);
        orderVO.setPayment(order.getPayment());
        orderVO.setPaymentType(order.getPaymentType());
        EnumClass.PaymentEnum paymentEnum = EnumClass.PaymentEnum.codeOf(order.getStatus());
        if (paymentEnum!=null){
            orderVO.setStatusDesc(paymentEnum.getDesc());
        }
        orderVO.setOrderNo(order.getOrderNo());




        return orderVO;
    }

    private ShippingVO assmbleShippingVO(Shipping shipping){
        ShippingVO shippingVO = new ShippingVO();
        if (shipping!=null){
            shippingVO.setReceiverAddress(shipping.getReceiverAddress());
            shippingVO.setReceiverCity(shipping.getReceiverCity());
            shippingVO.setReceiverDistrict(shipping.getReceiverDistrict());
            shippingVO.setReceiverMobile(shipping.getReceiverMobile());
            shippingVO.setReceiverName(shipping.getReceiverName());
            shippingVO.setReceiverPhone(shipping.getReceiverPhone());
            shippingVO.setReceiverProvince(shipping.getReceiverProvince());
            shippingVO.setReceiverZip(shipping.getReceiverZip());
        }
        return shippingVO;
    }

    private OrderItemVO assembleOrderItemVO(OrderItem orderItem){
        OrderItemVO orderItemVO = new OrderItemVO();
        if (orderItem!=null){
            orderItemVO.setQuantity(orderItem.getQuantity());
            orderItemVO.setCreateTime(DateUtils.dateToStr(orderItem.getCreateTime()));
            orderItemVO.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
            orderItemVO.setOrderNo(orderItem.getOrderNo());
            orderItemVO.setProductId(orderItem.getProductId());
            orderItemVO.setProductImage(orderItem.getProductImage());
            orderItemVO.setProductName(orderItem.getProductName());
            orderItemVO.setTotalPrice(orderItem.getTotalPrice());
        }

        return orderItemVO;
    }

    /**
     * 清空购物车中已选中的商品
     * */
    private void cleanCart(List<Cart> cartList){
        if (cartList!=null&&cartList.size()>0){
            cartMapper.batchDelete(cartList);
        }
    }

    /**
     * 扣库存
     * */

    private void reduceProductStoctStock(List<OrderItem> orderItemList){

        if (orderItemList!=null&&orderItemList.size()>0){
            for (OrderItem orderItem:orderItemList){
                Integer productId = orderItem.getProductId();
                Integer quantity = orderItem.getQuantity();
                Product product = productMapper.selectByPrimaryKey(productId);
                product.setStock(product.getStock()-quantity);
                productMapper.updateByPrimaryKey(product);
            }
        }
    }

    /**
     * 计算订单的总价格
     * */
    private BigDecimal getOrderPrice(List<OrderItem> orderItemList){
        BigDecimal bigDecimal = new BigDecimal("0");
        for (OrderItem orderItem:orderItemList){
            bigDecimal=BigDecimalUtils.add(bigDecimal.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return bigDecimal;
    }

    /**
     * 创建订单
     * */
    private Order create(Integer userId,Integer shippingId,BigDecimal orderTotalPrice){

        Order order = new Order();

        order.setOrderNo(generateOrderNO());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setStatus(EnumClass.OrderStatusEnum.ORDER_UN_PAY.getStatus());
        //订单金额
        order.setPayment(orderTotalPrice);
        order.setPostage(0);
        order.setPaymentType(EnumClass.PaymentEnum.ONLINE.getStatus());

        //保存订单
        int result = orderMapper.insert(order);
        if (result>0){
            return order;
        }

        return null;

    }

    /**
     * 生成订单编号
     * */
    private Long generateOrderNO(){
        return System.currentTimeMillis()+new Random().nextInt(100);
    }

    private ServerResponse getCartOrderItem(Integer userId,List<Cart> cartList) {
        if (cartList == null || cartList.size() == 0) {
            return ServerResponse.serverResponseByError("购物车为空");
        }
        List<OrderItem> orderItemList = Lists.newArrayList();
        for (Cart cart : cartList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (product == null) {
                return ServerResponse.serverResponseByError("id为" + cart.getProductId() + "的商品不存在");
            }
            if (product.getStatus()!= GoodsStates.PRODUCT_ONLINE.getRole()){
                return ServerResponse.serverResponseByError("id为"+product.getId()+"的商品已经下架");
            }
            if (product.getStock()<cart.getQuantity()){
                return ServerResponse.serverResponseByError("id为"+product.getId()+"的商品库存不足");
            }

            orderItem.setQuantity(cart.getQuantity());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setProductId(product.getId());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setProductName(product.getName());
            orderItem.setTotalPrice(BigDecimalUtils.mul(product.getPrice().doubleValue(),cart.getQuantity().doubleValue()));


            orderItemList.add(orderItem);
        }
        return ServerResponse.serverResponseBySuccess(orderItemList);
    }
}

