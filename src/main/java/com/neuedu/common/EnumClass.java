package com.neuedu.common;

public class EnumClass {

    public enum CartCheckedEnum{

        PRODUCT_CHECKED(1,"已勾选"),
        PRODUCT_UNCHECKED(0,"未勾选")
        ;
        private int status;
        private String desc;
        CartCheckedEnum() {

        }
        CartCheckedEnum(int status, String desc) {
            this.status = status;
            this.desc = desc;
        }
        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    public enum OrderStatusEnum{

        ORDER_CANCELED(0,"已取消"),
        ORDER_UN_PAY(10,"未付款"),
        ORDER_PAYED(20,"已付款"),
        ORDER_SEND(40,"已发货"),
        ORDER_SUCCESS(50,"交易成功"),
        ORDER_CLOSED(60,"交易关闭")
        ;
        private int status;
        private String desc;
        OrderStatusEnum() {

        }
        OrderStatusEnum(int status, String desc) {
            this.status = status;
            this.desc = desc;
        }
        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }


        //遍历枚举
        public static OrderStatusEnum codeOf(Integer code){
            for (OrderStatusEnum orderStatusEnum:values()){
                if (code==orderStatusEnum.getStatus()){
                    return orderStatusEnum;
                }
            }
            return null;
        }
    }

    public enum PaymentEnum{

        ONLINE(1,"线上支付")
        ;
        private int status;
        private String desc;
        PaymentEnum() {

        }
        PaymentEnum(int status, String desc) {
            this.status = status;
            this.desc = desc;
        }
        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }


        public static PaymentEnum codeOf(Integer code){
            for (PaymentEnum paymentEnum:values()){
                if (code==paymentEnum.getStatus()){
                    return paymentEnum;
                }
            }
            return null;
        }
    }

}
