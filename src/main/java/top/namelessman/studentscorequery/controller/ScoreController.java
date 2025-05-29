package top.namelessman.studentscorequery.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import top.namelessman.studentscorequery.entity.Student;
import top.namelessman.studentscorequery.mapper.StudentMapper;
import top.namelessman.studentscorequery.service.StudentService;
import top.namelessman.studentscorequery.util.CaptchaUtil;
import top.namelessman.studentscorequery.dto.AdminQueryParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ScoreController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentMapper studentMapper;

    @Value("${file.download.path}")
    private String fileDownloadPath;

    @GetMapping("/")
    public String index(Model model, HttpSession session) throws IOException {
        CaptchaUtil.Captcha captcha = CaptchaUtil.generateCaptcha();
        model.addAttribute("captchaImage", "data:image/png;base64," + captcha.getBase64Image());
        session.setAttribute("captcha", captcha.getCode());
        return "index";
    }


    @PostMapping("/score/check")
    public String checkScore(@RequestParam("name") String name,
                             @RequestParam("phone") String phone,
                             @RequestParam("captcha") String captcha,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request,
                             HttpSession session) {
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
            model.addAttribute("error", "验证码错误");
            try {
                CaptchaUtil.Captcha captcha1 = CaptchaUtil.generateCaptcha();
                model.addAttribute("captchaImage", "data:image/png;base64," + captcha1.getBase64Image());
                session.setAttribute("captcha", captcha1.getCode());

            } catch (IOException e) {
                e.printStackTrace();
            }

            return "index";
        }


        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        Student student = studentMapper.selectOne(queryWrapper);

        if (student == null) {
            model.addAttribute("error", "没有此学生");
            try {
                CaptchaUtil.Captcha captcha1 = CaptchaUtil.generateCaptcha();
                model.addAttribute("captchaImage", "data:image/png;base64," + captcha1.getBase64Image());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "index";
        }
        if (!student.getPhone().equals(phone)) {
            model.addAttribute("error", "手机号码有误，请检查，后四位为:" + student.getPhone().substring(7));
            try {
                CaptchaUtil.Captcha captcha1 = CaptchaUtil.generateCaptcha();
                session.setAttribute("captcha", captcha1.getCode());
                model.addAttribute("captchaImage", "data:image/png;base64," + captcha1.getBase64Image());

            } catch (IOException e) {
                e.printStackTrace();
            }
            return "index";
        }

        session.removeAttribute("captcha");
        studentService.markAsChecked(student.getId());
        boolean downloadable = new File(fileDownloadPath + "/" + name + ".sb3").exists();
        model.addAttribute("student", student);
        model.addAttribute("downloadable", downloadable);
        model.addAttribute("isDesktop", !isMobileDevice(request));

        return "result";
    }

    @PostMapping("/score/feedback")
    public String feedback(@RequestParam("id") Integer id,
                           @RequestParam(value = "feedback", required = false) String feedback,
                           Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        studentService.updateFeedback(id, feedback);
        Student student = studentService.getStudentById(id);
        boolean downloadable = new File(fileDownloadPath + "/" + student.getName() + ".sb3").exists();
        model.addAttribute("student", student);
        model.addAttribute("downloadable", downloadable);
        model.addAttribute("isDesktop", !isMobileDevice(request));
        model.addAttribute("suc", true);
        return "result";
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("name") String name) throws UnsupportedEncodingException {
        File file = new File(fileDownloadPath + "/" + name + ".sb3");
        if (!file.exists()) {
            return ResponseEntity.notFound().build(); // 文件不存在，返回 404
        }

        Resource resource = new FileSystemResource(file);

        HttpHeaders headers = new HttpHeaders();
        // 获取实际的文件名
        String filename = file.getName();
        // 使用 URLEncoder 对文件名进行编码
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/captcha")
    public ResponseEntity<String> getCaptcha(HttpSession session) throws IOException {
        CaptchaUtil.Captcha captcha = CaptchaUtil.generateCaptcha();
        session.setAttribute("captcha", captcha.getCode());

        return ResponseEntity.ok("data:image/png;base64," + captcha.getBase64Image());
    }

    @GetMapping("/admin")
    public String admin(AdminQueryParam params, Model model) {
        // 1. 密码验证
        if (!"Fucku_bug".equals(params.getPassword())) {
            // 密码不正确，重定向到首页
            return "redirect:/";
        }

        // 2. 准备所有教学班级列表，用于前端筛选复选框
        List<String> allTeachingClassNames = studentMapper.findAllTeachingClassNames();
        model.addAttribute("allTeachingClassNames", allTeachingClassNames);

        // --- 用于显示表格数据的查询条件 ---
        QueryWrapper<Student> displayStudentsQueryWrapper = new QueryWrapper<>();
        // 教学班级筛选 (对于显示数据)
        if (params.getTeachingClassNames() != null && !params.getTeachingClassNames().isEmpty()) {
            displayStudentsQueryWrapper.in("teaching_class_name", params.getTeachingClassNames());
        }
        // 是否查询筛选 (对于显示数据)
        if (params.getIsChecked() != null) {
            displayStudentsQueryWrapper.eq("is_checked", params.getIsChecked());
        }
        // 是否有留言筛选 (对于显示数据)
        if (params.getHasFeedback() != null) {
            if (params.getHasFeedback()) {
                displayStudentsQueryWrapper.isNotNull("feedback").ne("feedback", "");
            } else {
                displayStudentsQueryWrapper.and(wrapper -> wrapper.isNull("feedback").or().eq("feedback", ""));
            }
        }
        // 查询并添加到Model，供表格显示
        List<Student> students = studentMapper.selectList(displayStudentsQueryWrapper);
        model.addAttribute("students", students);
        model.addAttribute("filteredStudentCount", students.size()); // 筛选结果总人数


        // --- 用于计算查询率和留言率基数的查询条件 ---
        QueryWrapper<Student> baseCountQueryWrapper = new QueryWrapper<>();
        // 基数只根据教学班级筛选结果，不考虑其他筛选条件
        if (params.getTeachingClassNames() != null && !params.getTeachingClassNames().isEmpty()) {
            baseCountQueryWrapper.in("teaching_class_name", params.getTeachingClassNames());
        }
        long totalStudentsInSelectedClasses = studentMapper.selectCount(baseCountQueryWrapper);

        // --- 用于计算查询率分子的查询条件 ---
        QueryWrapper<Student> checkedCountQueryWrapper = new QueryWrapper<>();
        if (params.getTeachingClassNames() != null && !params.getTeachingClassNames().isEmpty()) {
            checkedCountQueryWrapper.in("teaching_class_name", params.getTeachingClassNames());
        }
        checkedCountQueryWrapper.eq("is_checked", true); // 仅统计已查询的
        long checkedStudents = studentMapper.selectCount(checkedCountQueryWrapper);

        // --- 用于计算留言率分子的查询条件 ---
        QueryWrapper<Student> feedbackCountQueryWrapper = new QueryWrapper<>();
        if (params.getTeachingClassNames() != null && !params.getTeachingClassNames().isEmpty()) {
            feedbackCountQueryWrapper.in("teaching_class_name", params.getTeachingClassNames());
        }
        feedbackCountQueryWrapper.isNotNull("feedback").ne("feedback", ""); // 仅统计有留言的
        long feedbackStudents = studentMapper.selectCount(feedbackCountQueryWrapper);


        // 计算并格式化比率
        double queryRate = (totalStudentsInSelectedClasses == 0) ? 0 : (double) checkedStudents / totalStudentsInSelectedClasses;
        double feedbackRate = (totalStudentsInSelectedClasses == 0) ? 0 : (double) feedbackStudents / totalStudentsInSelectedClasses;

        model.addAttribute("queryRate", String.format("%.2f%%", queryRate * 100));
        model.addAttribute("feedbackRate", String.format("%.2f%%", feedbackRate * 100));
        model.addAttribute("queryRateValue", queryRate * 100);
        model.addAttribute("feedbackRateValue", feedbackRate * 100);


        // 6. 将当前筛选参数传回页面，用于回显
        model.addAttribute("params", params);

        return "admin";
    }

    public boolean isMobileDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return false;
        }
        return userAgent.matches(".*(Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini).*");
    }
}
