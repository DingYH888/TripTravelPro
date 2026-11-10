package com.itbaizhan.travel;

import com.itbaizhan.travel.aop.LogAop;
import com.itbaizhan.travel.controller.backstage.ProductController;
import com.itbaizhan.travel.controller.frontdesk.FavoriteController;
import com.itbaizhan.travel.bean.Result;
import com.itbaizhan.travel.mapper.AdminMapper;
import com.itbaizhan.travel.mapper.MemberMapper;
import com.itbaizhan.travel.mapper.RoleMapper;
import com.itbaizhan.travel.pojo.Member;
import com.itbaizhan.travel.pojo.WangEditorResult;
import com.itbaizhan.travel.service.AdminService;
import com.itbaizhan.travel.service.FavoriteService;
import com.itbaizhan.travel.service.MemberService;
import com.itbaizhan.travel.service.ProductService;
import com.itbaizhan.travel.service.RoleService;
import com.itbaizhan.travel.util.MailUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
 * 问题修复验证用例
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
    private FavoriteService favoriteService;
    @InjectMocks
    private FavoriteController favoriteController;

    @Mock
    private ProductService productService;
    @InjectMocks
    private ProductController productController;

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
     * 收藏线路后应重定向到线路详情页，不再依赖 Referer 请求头
     */
    @Test
    void addFavorite_shouldRedirectToProductDetail() {
        MockHttpSession session = new MockHttpSession();
        Member member = new Member();
        member.setMid(9);
        session.setAttribute("member", member);

        String view = favoriteController.add(3, session);

        assertEquals("redirect:/frontdesk/product/routeDetail?pid=3", view);
        verify(favoriteService).addFavorite(3, 9);
    }

    /**
     * 取消收藏后应重定向到线路详情页
     */
    @Test
    void delFavorite_shouldRedirectToProductDetail() {
        MockHttpSession session = new MockHttpSession();
        Member member = new Member();
        member.setMid(9);
        session.setAttribute("member", member);

        String view = favoriteController.del(3, session);

        assertEquals("redirect:/frontdesk/product/routeDetail?pid=3", view);
        verify(favoriteService).delFavorite(3, 9);
    }

    /**
     * 后台启停用产品后应重定向到产品列表，不再依赖 Referer 请求头
     */
    @Test
    void updateProductStatus_shouldRedirectToProductList() {
        String view = productController.updateStatus(3);

        assertEquals("redirect:/backstage/product/all", view);
        verify(productService).updateStatus(3);
    }

    /**
     * 上传图片应保存到上传目录的 img/product 下，并返回与前台一致的访问路径
     */
    @Test
    void uploadImage_shouldSaveToUploadDirAndReturnPublicPath(@TempDir Path tempDir) throws Exception {
        ReflectionTestUtils.setField(productController, "uploadPath", tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "route.png",
                "image/png", "png-content".getBytes(StandardCharsets.UTF_8));

        WangEditorResult result = productController.upload(file);

        assertEquals(0, result.getErrno());
        String url = result.getData()[0];
        assertTrue(url.startsWith("/upload/img/product/"));
        File saved = new File(tempDir.toFile(), "img/product/" + url.substring("/upload/img/product/".length()));
        assertTrue(saved.exists());
        assertTrue(saved.getName().endsWith(".png"));
    }

    /**
     * 上传非图片文件应被拒绝，避免任意文件写入
     */
    @Test
    void uploadNonImage_shouldBeRejected(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(productController, "uploadPath", tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "shell.jsp",
                "application/octet-stream", "<% %>".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalArgumentException.class, () -> productController.upload(file));
        assertFalse(new File(tempDir.toFile(), "img/product").exists());
    }

    /**
     * 未认证时访问后台，日志切面不应抛出空指针异常
     */
    @Test
    void logAop_withoutAuthentication_shouldNotThrow() {
        SecurityContextHolder.clearContext();
        LogAop logAop = new LogAop();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/backstage/admin/all");
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
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/backstage/admin/all");
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
}
