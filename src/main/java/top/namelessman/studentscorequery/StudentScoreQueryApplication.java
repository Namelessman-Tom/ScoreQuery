package top.namelessman.studentscorequery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("top.namelessman.studentscorequery.mapper")
public class StudentScoreQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentScoreQueryApplication.class, args);
    }

}
