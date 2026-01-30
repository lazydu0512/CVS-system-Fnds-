package cn.edu.seig.fnds;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.edu.seig.fnds.mapper") // 这一行必须加
public class CvsPortApplication {

    public static void main(String[] args) {
        SpringApplication.run(CvsPortApplication.class, args);
    }

}
