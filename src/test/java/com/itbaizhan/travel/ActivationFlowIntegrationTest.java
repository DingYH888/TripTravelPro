package com.itbaizhan.travel;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itbaizhan.travel.bean.Result;
import com.itbaizhan.travel.mapper.MemberMapper;
import com.itbaizhan.travel.pojo.Member;
import com.itbaizhan.travel.service.MemberService;
import com.itbaizhan.travel.util.MailUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 账户激活全链路集成测试：注册 -> 生成激活链接 -> 激活 -> 链接失效
 * 需要本机 MySQL 正常运行（与 application.yml 配置一致）
 */
@SpringBootTest
class ActivationFlowIntegrationTest {

    private static final String TEST_USERNAME = "itflow_user";
    private static final String TEST_EMAIL = "itflow@example.com";

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberMapper memberMapper;
    @MockBean
    private MailUtils mailUtils;

    @BeforeEach
    void setUp() {
        removeTestMember();
    }

    @AfterEach
    void tearDown() {
        removeTestMember();
    }

    private void removeTestMember() {
        memberMapper.delete(new QueryWrapper<Member>().eq("username", TEST_USERNAME));
    }

    @Test
    void fullActivationFlow() {
        when(mailUtils.sendMail(anyString(), anyString(), anyString())).thenReturn(true);

        // 1.注册
        Member member = new Member();
        member.setUsername(TEST_USERNAME);
        member.setPassword("123456");
        member.setPhoneNum("13911112222");
        member.setEmail(TEST_EMAIL);
        Result result = memberService.register(member);
        assertTrue(result.isFlag());

        // 2.校验邮件中的激活链接
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mailUtils).sendMail(eq(TEST_EMAIL), captor.capture(), anyString());
        Matcher matcher = Pattern
                .compile("http://localhost:8080/frontdesk/member/active\\?activeCode=([0-9a-f-]{36})")
                .matcher(captor.getValue());
        assertTrue(matcher.find(), "邮件中应包含格式正确的激活链接");
        String activeCode = matcher.group(1);

        // 3.校验数据库存储状态
        Member stored = memberMapper.selectOne(new QueryWrapper<Member>().eq("username", TEST_USERNAME));
        assertFalse(stored.isActive());
        assertEquals(activeCode, stored.getActiveCode());
        assertTrue(stored.getActiveCodeExpire() > System.currentTimeMillis());

        // 4.点击链接激活
        assertTrue(memberService.active(activeCode).contains("激活成功"));
        Member activated = memberMapper.selectById(stored.getMid());
        assertTrue(activated.isActive());
        assertNull(activated.getActiveCode());
        assertNull(activated.getActiveCodeExpire());

        // 5.重复使用同一链接应提示失效
        assertTrue(memberService.active(activeCode).contains("无效或已使用"));
    }
}
