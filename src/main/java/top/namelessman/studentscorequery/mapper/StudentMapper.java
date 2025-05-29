// src/main/java/com/example/score/mapper/StudentMapper.java
package top.namelessman.studentscorequery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.namelessman.studentscorequery.entity.Student;

import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    @Update("UPDATE student SET is_checked = true WHERE id = #{id}")
    void markAsChecked(@Param("id") Integer id);

    @Update("UPDATE student SET feedback = #{feedback} WHERE id = #{id}")
    void updateFeedback(@Param("id") Integer id, @Param("feedback") String feedback);

    @Select("SELECT DISTINCT class_name FROM student")
    List<String> findAllClassNames();

    // 新增：获取所有不重复的教学班级名称
    @Select("SELECT DISTINCT teaching_class_name FROM student WHERE teaching_class_name IS NOT NULL AND teaching_class_name != ''")
    List<String> findAllTeachingClassNames();

}
