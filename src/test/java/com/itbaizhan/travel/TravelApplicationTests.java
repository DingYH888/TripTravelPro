package com.itbaizhan.travel;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.travel.pojo.Admin;
import com.itbaizhan.travel.service.AdminService;
import com.itbaizhan.travel.util.MailUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TravelApplicationTests {
    @Autowired
    private AdminService adminService;
    @Autowired
    private MailUtils mailUtils;

//    @Test
//    void contextLoads() {
//        Page<Admin> page = adminService.findPage(1, 5);
//        System.out.println(page);
//    }

//    @Test
//    void sendMail() {
//        mailUtils.sendMail("2728093475@qq.com","这是一封测试邮件","测试");
//    }

}
