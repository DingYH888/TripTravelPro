package com.itbaizhan.travel.controller.backstage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.travel.bean.PermissionWithStatus;
import com.itbaizhan.travel.pojo.Role;
import com.itbaizhan.travel.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/backstage/role")
public class RoleController {
    @Autowired
    private RoleService roleService;


    @RequestMapping("/all")
    @PreAuthorize("hasAnyAuthority('/role/all')")
    public ModelAndView all(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Role> rolePage = roleService.findPage(page, size);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("rolePage", rolePage);
        modelAndView.setViewName("/backstage/role_all");
        return modelAndView;
    }
    @RequestMapping("/add")
    public String add(Role role) {
        roleService.add(role);
        return "redirect:/backstage/role/all";
    }

    @RequestMapping("/edit")
    public ModelAndView edit(Integer rid) {
        Role role = roleService.findById(rid);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("role", role);
        modelAndView.setViewName("/backstage/role_edit");
        return modelAndView;
    }


    @RequestMapping("/update")
    public String update(Role role) {
        roleService.update(role);
        return "redirect:/backstage/role/all";
    }

    @RequestMapping("/delete")
    public String delete(Integer rid){
        roleService.delete(rid);
        return "redirect:/backstage/role/all";
    }

    @RequestMapping("/findPermission")
    public ModelAndView findRole(Integer rid) {
        List<PermissionWithStatus> permissions = roleService.findPermission(rid);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("permissions", permissions);
        modelAndView.addObject("rid", rid);
        modelAndView.setViewName("/backstage/role_permission");
        return modelAndView;
    }

    @RequestMapping("/updatePermission")
    public String managePermission(Integer rid,Integer[] ids){
        roleService.updatePermissions(rid,ids);
        return "redirect:/backstage/role/all";
    }

}
