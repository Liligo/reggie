package com.liligo.reggie.dto;

import com.liligo.reggie.entity.OrderDetail;
import com.liligo.reggie.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
