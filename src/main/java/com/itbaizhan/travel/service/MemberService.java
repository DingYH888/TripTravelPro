package com.itbaizhan.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.itbaizhan.travel.bean.Result;
import com.itbaizhan.travel.mapper.MemberMapper;
import com.itbaizhan.travel.pojo.Member;
import com.itbaizhan.travel.util.MailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
public class MemberService {
    // 激活链接有效期：24小时
    private static final long ACTIVE_CODE_TTL = 24 * 60 * 60 * 1000L;

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private MailUtils mailUtils;
    
    @Value("${project.path}")
    private String projectPath;


    // 注册
    public Result register(Member member){
        // 1.保存用户
        // 验证用户名是否重复
        QueryWrapper<Member> queryWrapper = new QueryWrapper();
        queryWrapper.eq("username",member.getUsername());
        List<Member> members = memberMapper.selectList(queryWrapper);
        if (members.size() > 0){
            return new Result(false,"用户名已存在");
        }


        // 验证手机是否重复
        QueryWrapper<Member> queryWrapper1 = new QueryWrapper();
        queryWrapper1.eq("phoneNum",member.getPhoneNum());
        List<Member> members1 = memberMapper.selectList(queryWrapper1);
        if (members1.size() > 0){
            return new Result(false,"手机已存在");
        }


        // 验证邮箱是否重复
        QueryWrapper<Member> queryWrapper2 = new QueryWrapper();
        queryWrapper2.eq("email",member.getEmail());
        List<Member> members2 = memberMapper.selectList(queryWrapper2);
        if (members2.size() > 0){
            return new Result(false,"邮箱已存在");
        }


        // 加密密码
        String password = member.getPassword();
        password = encoder.encode(password);
        member.setPassword(password);
        // 设置用户状态为false
        member.setActive(false);


        // 2.发送激活邮件
        // 生成激活码
        String activeCode = UUID.randomUUID().toString();
        // 给用户的邮箱发送一封邮件，该邮件包含一个链接，链接中包含激活码
        String activeUrl = projectPath + "/activate?code="
                + URLEncoder.encode(activeCode, StandardCharsets.UTF_8);
        String text = "恭喜您注册成功！<br/><br/>" +
              "请点击下方链接激活您的账号（链接24小时内有效）：<br/>" +
                "<a href='" + activeUrl + "' style='color:#1890ff;text-decoration:none;'>点击激活账号</a>" +
              "（如链接无法点击，请复制以下地址到浏览器地址栏打开）<br/><br/>" +
                      activeUrl + "<br/><br/>";
        // 保存激活码及其过期时间
        member.setActiveCode(activeCode);
        member.setActiveCodeExpire(System.currentTimeMillis() + ACTIVE_CODE_TTL);
        // 保存用户
        memberMapper.insert(member);

        // 发送激活邮件，发送失败时删除未激活账号，避免用户无法重注册也无法登录
        boolean sendResult = mailUtils.sendMail(member.getEmail(), text, "去哪旅游网激活邮件");
        if (!sendResult){
            memberMapper.deleteById(member.getMid());
            return new Result(false,"激活邮件发送失败，请稍后重试");
        }
        return new Result(true,"注册成功！");
    }

    // 激活用户
    public String active(String activeCode){
        String loginUrl = projectPath + "/login";
        // 1.链接参数缺失
        if (activeCode == null || activeCode.trim().isEmpty()){
            return "激活链接不完整，请确认已复制完整的邮件链接。";
        }
        // 2.根据激活码查询用户
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activeCode", activeCode.trim());
        Member member = memberMapper.selectOne(queryWrapper);
        if (member == null){
            return "激活链接无效或已使用，请重新注册获取新的激活邮件，或<a href='"+loginUrl+"'>前往登录</a>。";
        }
        // 3.重复激活
        if (member.isActive()){
            return "该账号已激活，无需重复操作，请直接<a href='"+loginUrl+"'>登录</a>。";
        }
        // 4.激活码过期
        Long expire = member.getActiveCodeExpire();
        if (expire != null && expire < System.currentTimeMillis()){
            return "激活链接已过期（激活码有效期为24小时），请重新注册获取新的激活邮件。";
        }
        // 5.激活成功，同时清除激活码，保证链接只能使用一次
        UpdateWrapper<Member> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("mid", member.getMid())
                .set("active", true)
                .set("activeCode", null)
                .set("activeCodeExpire", null);
        memberMapper.update(null, updateWrapper);
        return "激活成功，请<a href='"+loginUrl+"'>登录</a>";
    }

    public Result login(String name,String password){
        Member member = null;

        // 查询用户名
        if (member == null){
            QueryWrapper<Member> queryWrapper = new QueryWrapper();
            queryWrapper.eq("username",name);
            member = memberMapper.selectOne(queryWrapper);
        }

        // 查询手机
        if (member == null){
            QueryWrapper<Member> queryWrapper = new QueryWrapper();
            queryWrapper.eq("phoneNum",name);
            member = memberMapper.selectOne(queryWrapper);
        }

        // 查询邮箱
        if (member == null){
            QueryWrapper<Member> queryWrapper = new QueryWrapper();
            queryWrapper.eq("email",name);
            member = memberMapper.selectOne(queryWrapper);
        }


        // 没有查询到用户
        if(member == null){
            return new Result(false,"用户名或密码错误");
        }

        // 验证密码
        boolean flag = encoder.matches(password, member.getPassword());
        if(!flag){
            return new Result(false,"用户名或密码错误");
        }

        // 验证是否激活
        if(!member.isActive()){
            return new Result(false,"用户未激活，请登录邮箱激活用户！");
        }

        return new Result(true,"登录成功！",member);
    }

}
