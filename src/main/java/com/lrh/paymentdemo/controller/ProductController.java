package com.lrh.paymentdemo.controller;

import com.lrh.paymentdemo.entity.Product;
import com.lrh.paymentdemo.result.R;
import com.lrh.paymentdemo.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.controller
 * @ClassName: ProductController
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/26 14:15
 */
@CrossOrigin
@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @ApiOperation("测试接口")
    @GetMapping("/test")
    public R<Map<String, Object>> test() {
        Map<String, Object> map = new HashMap<>();
        map.put("rep", "ok");
        map.put("time", new Date());
        return R.ok(map);
    }

    @ApiOperation("商品列表")
    @GetMapping("/list")
    public R<Map<String,Object>> getProductList() {
        List<Product> list = productService.list();
        Map<String,Object> map = new HashMap<>();
        map.put("productList",list);
        return R.ok(map);
    }


}
