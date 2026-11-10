package com.itbaizhan.travel.controller.backstage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.travel.pojo.Category;
import com.itbaizhan.travel.pojo.Product;
import com.itbaizhan.travel.pojo.WangEditorResult;
import com.itbaizhan.travel.service.CategoryService;
import com.itbaizhan.travel.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Controller
@RequestMapping("/backstage/product")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Value("${upload.path}")
    private String uploadPath;

    // 允许上传的图片后缀
    private static final List<String> ALLOWED_SUFFIX = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp");


    @RequestMapping("/all")
    public ModelAndView all(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size) {
        Page<Product> productPage = productService.findPage(page, size);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("productPage", productPage);
        modelAndView.setViewName("/backstage/product_all");
        return modelAndView;
    }

    @RequestMapping("/addPage")
    public ModelAndView addPage() {
        // 查询所有产品类别
        List<Category> categoryList = categoryService.findAll();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("categoryList", categoryList);
        modelAndView.setViewName("/backstage/product_add");
        return modelAndView;
    }


    @RequestMapping("/add")
    public String add(Product product) {
        productService.add(product);
        return "redirect:/backstage/product/all";
    }

    @RequestMapping(value = "/upload")
    @ResponseBody
    public WangEditorResult upload(MultipartFile file) throws Exception {
        // 拿到上传文件名，并校验后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.lastIndexOf('.') > -1) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }
        if (!ALLOWED_SUFFIX.contains(suffix)) {
            throw new IllegalArgumentException("只支持上传 jpg/jpeg/png/gif/bmp/webp 格式的图片");
        }

        // 1.设置上传文件夹（项目运行目录下的 upload/img/product，与前台图片访问路径保持一致）
        File dir = new File(uploadPath, "img/product");
        //2.判断该文件夹是否存在，如果不存在，新建文件夹
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("创建上传目录失败：" + dir.getAbsolutePath());
        }
        // 以随机名保存，避免文件名冲突与路径穿越
        String filename = UUID.randomUUID() + suffix;
        File newFile = new File(dir, filename);
        // 将上传的文件写到空文件中（使用绝对路径，避免相对路径被解析到临时目录）
        file.transferTo(newFile.getAbsoluteFile());


        WangEditorResult wangEditorResult = new WangEditorResult();
        String[] data = {"/upload/img/product/" + filename};
        wangEditorResult.setErrno(0);
        wangEditorResult.setData(data);
        return wangEditorResult;
    }

    @RequestMapping("/edit")
    public ModelAndView edit(Integer pid) {
        // 查询被修改的产品
        Product product = productService.findOne(pid);
        // 查询所有产品类别
        List<Category> categoryList = categoryService.findAll();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("product", product);
        modelAndView.addObject("categoryList", categoryList);
        modelAndView.setViewName("/backstage/product_edit");
        return modelAndView;
    }


    @RequestMapping("/update")
    public String update(Product product) {
        productService.update(product);
        return "redirect:/backstage/product/all";
    }

    @RequestMapping("/updateStatus")
    public String updateStatus(Integer pid) {
        productService.updateStatus(pid);
        return "redirect:/backstage/product/all";
    }

}
