// src/main/java/com/example/score/mapper/StudentMapper.java
package top.namelessman.studentscorequery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.namelessman.studentscorequery.entity.Student;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    @Update("UPDATE student SET is_checked = true WHERE id = #{id}")
    void markAsChecked(@Param("id") Integer id);

    @Update("UPDATE student SET feedback = #{feedback} WHERE id = #{id}")
    void updateFeedback(@Param("id") Integer id, @Param("feedback") String feedback);
}