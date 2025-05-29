// src/main/java/com/example/score/service/StudentService.java
package top.namelessman.studentscorequery.service;


import com.baomidou.mybatisplus.extension.service.IService;
import top.namelessman.studentscorequery.entity.Student;


public interface StudentService extends IService<Student> {
    Student getStudentByName(String name);
    void markAsChecked(Integer id);
    Student getStudentById(Integer id);

    void updateFeedback(Integer id,String feedback);
}