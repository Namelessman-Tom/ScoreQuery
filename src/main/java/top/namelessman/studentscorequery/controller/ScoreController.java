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

        // 3. 构建查询条件
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();

        // 教学班级筛选 (复选框，默认全不选则显示所有学生)
        if (params.getTeachingClassNames() != null && !params.getTeachingClassNames().isEmpty()) { // <-- 已经改用 getTeachingClassNames()
            queryWrapper.in("teaching_class_name", params.getTeachingClassNames()); // <-- 已经改用 getTeachingClassNames()
        }

        // 是否查询筛选
        if (params.getIsChecked() != null) {
            queryWrapper.eq("is_checked", params.getIsChecked());
        }

        // 是否有留言筛选
        if (params.getHasFeedback() != null) {
            if (params.getHasFeedback()) { // 有留言 (feedback IS NOT NULL AND feedback != '')
                queryWrapper.isNotNull("feedback").ne("feedback", "");
            } else { // 无留言 (feedback IS NULL OR feedback = '')
                queryWrapper.and(wrapper -> wrapper.isNull("feedback").or().eq("feedback", ""));
            }
        }

        // 4. 查询学生数据 (筛选结果)
        List<Student> students = studentMapper.selectList(queryWrapper);
        model.addAttribute("students", students);
        model.addAttribute("filteredStudentCount", students.size()); // 筛选结果总人数

        // 5. 计算统计数据 (查询率和留言率)
        long totalStudentsInSelectedClasses = 0; // 选定教学班级总人数
        if (params.getTeachingClassNames() != null && !params.getTeachingClassNames().isEmpty()) { // <-- 已经改用 getTeachingClassNames()
            // 如果指定了教学班级，就统计这些教学班级的总人数
            totalStudentsInSelectedClasses = studentMapper.selectCount(
                    new QueryWrapper<Student>().in("teaching_class_name", params.getTeachingClassNames()) // <-- 已经改用 getTeachingClassNames()
            );
        } else {
            // 否则统计所有学生总人数
            totalStudentsInSelectedClasses = studentMapper.selectCount(null);
        }

        // 从筛选结果中统计已查询和有留言的人数
        long checkedStudents = students.stream().filter(s -> s.getChecked() != null && s.getChecked()).count();
        long feedbackStudents = students.stream().filter(s -> s.getFeedback() != null && !s.getFeedback().trim().isEmpty()).count();

        double queryRate = (totalStudentsInSelectedClasses == 0) ? 0 : (double) checkedStudents / totalStudentsInSelectedClasses;
        double feedbackRate = (totalStudentsInSelectedClasses == 0) ? 0 : (double) feedbackStudents / totalStudentsInSelectedClasses;

        // 格式化百分比
        model.addAttribute("queryRate", String.format("%.2f%%", queryRate * 100));
        model.addAttribute("feedbackRate", String.format("%.2f%%", feedbackRate * 100));
        model.addAttribute("queryRateValue", queryRate * 100); // 进度条需要数值
        model.addAttribute("feedbackRateValue", feedbackRate * 100); // 进度条需要数值


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
