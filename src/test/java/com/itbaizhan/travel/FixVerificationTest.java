package com.itbaizhan.travel;

import com.itbaizhan.travel.aop.LogAop;
import com.itbaizhan.travel.bean.Result;
import com.itbaizhan.travel.mapper.AdminMapper;
import com.itbaizhan.travel.mapper.MemberMapper;
import com.itbaizhan.travel.mapper.RoleMapper;
import com.itbaizhan.travel.pojo.Member;
import com.itbaizhan.travel.service.AdminService;
import com.itbaizhan.travel.service.MemberService;
import com.itbaizhan.travel.service.RoleService;
import com.itbaizhan.travel.util.MailUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 问题修复验证用例（服务层与日志切面）。
 */
@ExtendWith(MockitoExtension.class)
class FixVerificationTest {

    @Mock
    private RoleMapper roleMapper;
    @InjectMocks
    private RoleService roleService;

    @Mock
    private AdminMapper adminMapper;
    @InjectMocks
    private AdminService adminService;

    @Mock
    private MemberMapper memberMapper;
    @Mock
    private BCryptPasswordEncoder encoder;
    @Mock
    private MailUtils mailUtils;
    @InjectMocks
    private MemberService memberService;

    /**
     * 后台分配权限时未勾选任何权限，ids 为 null，不应抛出空指针异常
     */
    @Test
    void updatePermissions_withNullIds_shouldClearPermissionsWithoutException() {
        roleService.updatePermissions(1, null);

        verify(roleMapper).deleteRoleAllPermission(1);
        verify(roleMapper, never()).addRolePermission(anyInt(), anyInt());
    }

    /**
     * 后台分配角色时未勾选任何角色，ids 为 null，不应抛出空指针异常
     */
    @Test
    void updateRoles_withNullIds_shouldClearRolesWithoutException() {
        adminService.updateRoles(1, null);

        verify(adminMapper).deleteAdminAllRoles(1);
        verify(adminMapper, never()).addAdminRole(anyInt(), anyInt());
    }

    /**
     * 未认证时记录后台操作日志，日志切面不应抛出空指针异常
     */
    @Test
    void logAop_withoutAuthentication_shouldNotThrow() {
        SecurityContextHolder.clearContext();
        LogAop logAop = new LogAop();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/products");
        request.setAttribute("visitTime", new Date());
        ReflectionTestUtils.setField(logAop, "request", request);

        logAop.doAfter();
    }

    /**
     * 缺少访问时间属性时，日志切面不应抛出空指针异常
     */
    @Test
    void logAop_withoutVisitTime_shouldNotThrow() {
        SecurityContextHolder.clearContext();
        LogAop logAop = new LogAop();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/products");
        ReflectionTestUtils.setField(logAop, "request", request);

        logAop.doAfter();
        logAop.afterThrowing(new RuntimeException("mock"));
    }

    /**
     * 激活邮件发送失败时应删除未激活账号，返回注册失败，允许用户重新注册
     */
    @Test
    void register_whenMailSendFailed_shouldRemoveMemberAndReturnFailure() {
        ReflectionTestUtils.setField(memberService, "projectPath", "http://localhost:8080");
        Member member = new Member();
        member.setMid(100);
        member.setUsername("tester");
        member.setPassword("123456");
        member.setPhoneNum("13800000000");
        member.setEmail("tester@example.com");
        when(encoder.encode(anyString())).thenReturn("$2a$encoded");
        when(mailUtils.sendMail(anyString(), anyString(), anyString())).thenReturn(false);

        Result result = memberService.register(member);

        assertFalse(result.isFlag());
        verify(memberMapper).deleteById(100);
    }

    /**
     * 邮件发送成功时正常保存账号
     */
    @Test
    void register_whenMailSendSuccess_shouldKeepMember() {
        ReflectionTestUtils.setField(memberService, "projectPath", "http://localhost:8080");
        Member member = new Member();
        member.setMid(101);
        member.setUsername("tester2");
        member.setPassword("123456");
        member.setPhoneNum("13800000001");
        member.setEmail("tester2@example.com");
        when(encoder.encode(anyString())).thenReturn("$2a$encoded");
        when(mailUtils.sendMail(anyString(), anyString(), anyString())).thenReturn(true);

        Result result = memberService.register(member);

        assertTrue(result.isFlag());
        verify(memberMapper).insert(member);
        verify(memberMapper, never()).deleteById(anyInt());
    }

    /**
     * 注册时生成的激活链接指向新前台页面，且激活码带 24 小时有效期
     */
    @Test
    void register_shouldGenerateValidActivationLinkAndExpiry() {
        ReflectionTestUtils.setField(memberService, "projectPath", "http://localhost:8080");
        Member member = new Member();
        member.setMid(102);
        member.setUsername("tester3");
        member.setPassword("123456");
        member.setPhoneNum("13800000002");
        member.setEmail("link@example.com");
        when(encoder.encode(anyString())).thenReturn("$2a$encoded");
        when(mailUtils.sendMail(anyString(), anyString(), anyString())).thenReturn(true);

        memberService.register(member);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailUtils).sendMail(eq("link@example.com"), textCaptor.capture(), anyString());
        String text = textCaptor.getValue();
        assertTrue(text.contains("http://localhost:8080/activate?code="));
        assertTrue(member.getActiveCode().matches("[0-9a-f-]{36}"));
        assertTrue(text.contains(member.getActiveCode()));
        long ttl = member.getActiveCodeExpire() - System.currentTimeMillis();
        assertTrue(ttl > 23 * 60 * 60 * 1000L && ttl <= 24 * 60 * 60 * 1000L);
    }

    /**
     * 激活码为空白时应提示链接不完整
     */
    @Test
    void active_withBlankCode_shouldPromptIncompleteLink() {
        assertTrue(memberService.active(" ").contains("不完整"));
        verify(memberMapper, never()).selectOne(any());
    }

    /**
     * 激活码不存在（已被使用或伪造）时给出明确引导
     */
    @Test
    void active_withUnknownCode_shouldPromptInvalidOrUsed() {
        when(memberMapper.selectOne(any())).thenReturn(null);

        String message = memberService.active("not-exists-code");

        assertTrue(message.contains("无效或已使用"));
        assertTrue(message.contains("/login"));
        verify(memberMapper, never()).update(isNull(), any());
    }

    /**
     * 账号已激活时不应重复激活
     */
    @Test
    void active_whenAlreadyActivated_shouldPromptAlreadyActive() {
        Member member = new Member();
        member.setMid(103);
        member.setActive(true);
        member.setActiveCode("code-103");
        when(memberMapper.selectOne(any())).thenReturn(member);

        String message = memberService.active("code-103");

        assertTrue(message.contains("已激活"));
        verify(memberMapper, never()).update(isNull(), any());
    }

    /**
     * 激活码超过 24 小时应提示已过期
     */
    @Test
    void active_whenExpired_shouldPromptExpired() {
        Member member = new Member();
        member.setMid(104);
        member.setActive(false);
        member.setActiveCode("code-104");
        member.setActiveCodeExpire(System.currentTimeMillis() - 1000);
        when(memberMapper.selectOne(any())).thenReturn(member);

        String message = memberService.active("code-104");

        assertTrue(message.contains("已过期"));
        verify(memberMapper, never()).update(isNull(), any());
    }

    /**
     * 激活成功时应同时清除激活码，保证链接只能使用一次
     */
    @Test
    void active_whenValid_shouldActivateAndClearCode() {
        Member member = new Member();
        member.setMid(105);
        member.setActive(false);
        member.setActiveCode("code-105");
        member.setActiveCodeExpire(System.currentTimeMillis() + 60 * 60 * 1000L);
        when(memberMapper.selectOne(any())).thenReturn(member);

        String message = memberService.active("code-105");

        assertTrue(message.contains("激活成功"));
        verify(memberMapper).update(isNull(), any());
    }
}
