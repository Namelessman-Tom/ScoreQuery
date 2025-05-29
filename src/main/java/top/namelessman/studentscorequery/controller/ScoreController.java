package top.namelessman.studentscorequery.controller;// src/main/java/com/example/score/controller/ScoreController.java


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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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


    public boolean isMobileDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return false;
        }
        return userAgent.matches(".*(Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini).*");
    }
}