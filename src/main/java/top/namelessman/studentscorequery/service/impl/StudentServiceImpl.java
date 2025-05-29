// src/main/java/com/example/score/service/impl/StudentServiceImpl.java
package top.namelessman.studentscorequery.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.namelessman.studentscorequery.entity.Student;
import top.namelessman.studentscorequery.mapper.StudentMapper;
import top.namelessman.studentscorequery.service.StudentService;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public Student getStudentByName(String name) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        return studentMapper.selectOne(queryWrapper);
    }

    @Override
    public void markAsChecked(Integer id) {
        studentMapper.markAsChecked(id);
    }

    @Override
    public Student getStudentById(Integer id) {
        return studentMapper.selectById(id);
    }

    @Override
    public void updateFeedback(Integer id, String feedback) {
        studentMapper.updateFeedback(id, feedback);
    }
}